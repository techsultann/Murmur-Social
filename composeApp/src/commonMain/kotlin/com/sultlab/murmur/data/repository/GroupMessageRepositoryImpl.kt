package com.sultlab.murmur.data.repository

import co.touchlab.kermit.Logger
import com.sultlab.murmur.data.local.DeviceHashStore
import com.sultlab.murmur.data.local.dao.GroupMessageDao
import com.sultlab.murmur.data.model.GroupMemberRole
import com.sultlab.murmur.data.mapper.toEntity
import com.sultlab.murmur.data.mapper.toGroupMessage
import com.sultlab.murmur.data.remote.GroupMessage
import com.sultlab.murmur.data.remote.GroupMessageDto
import com.sultlab.murmur.data.remote.ReplyPreviewDto
import com.sultlab.murmur.data.remote.ToggleReactionResponse
import com.sultlab.murmur.domain.repository.GroupMessageRepository
import com.sultlab.murmur.domain.repository.GroupRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.serializer
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

class GroupMessageRepositoryImpl(
    private val supabase: SupabaseClient,
    private val dao: GroupMessageDao,
    private val deviceHashStore: DeviceHashStore,
    private val groupRepository: GroupRepository,
    private val scope: CoroutineScope,
) : GroupMessageRepository {

    private val logger = Logger.withTag("GroupMessageRepository")
    private val channelsByGroup = mutableMapOf<String, Job>()
    private val adminHashesByGroup = mutableMapOf<String, Set<String>>()
    private val json = Json { ignoreUnknownKeys = true }
    private var initJob: Job? = null

    // ── Observe messages ──────────────────────────────────────
    // Room is the single source of truth — the UI observes this Flow.
    // Realtime events write to Room, Room emits to the UI.

    override fun observeMessages(groupId: String, adminDeviceHashes: Set<String>): Flow<List<GroupMessage>> =
        dao.observeMessages(groupId).map { entities ->
            entities.map { it.toGroupMessage(adminDeviceHashes) }
        }

    // ── Load from Supabase then cache in Room ─────────────────
    // Called once when entering the group chat. Fetches the last 72h
    // of messages, caches them locally, then realtime keeps it live.

    override suspend fun loadAndCache(groupId: String) {
        try {
            val since = (Clock.System.now() - 72.hours).toString()

            val dtos = supabase.postgrest["group_messages"]
                .select {
                    filter {
                        eq("group_id", groupId)
                        eq("is_deleted", false)
                        gte("created_at", since)
                    }
                    order("created_at", Order.ASCENDING)
                }
                .decodeList<GroupMessageDto>()

            val replyIds = dtos.mapNotNull { it.replyToId }.distinct()
            val replyPreviews = if (replyIds.isNotEmpty()) {
                supabase.postgrest["group_messages"]
                    .select {
                        filter { isIn("id", replyIds) }
                    }
                    .decodeList<ReplyPreviewDto>()
                    .associate { it.id to it.content }
            } else emptyMap()

            // Enforce local 72h cutoff before caching
            val cutoff = cutoffMillis()
            dao.deleteOlderThan(groupId, cutoff)

            // Resolve admin status
            val adminHashes = adminHashesByGroup[groupId] ?: emptySet()

            logger.d { "Caching ${dtos.size} messages for group $groupId. Sample hash: ${dtos.firstOrNull()?.deviceHash}" }

            // Upsert everything from server into Room
            dao.upsertAll(dtos.map { it.toEntity(adminHashes, replyPreviews) })
        } catch (e: Exception) {
            logger.e(e) { "Error in loadAndCache for group: $groupId" }
            throw e
        }
    }


    override suspend fun sendMessage(
        groupId: String,
        content: String,
        replyToId: String?,
    ) {
        val deviceHash = deviceHashStore.getDeviceHash()
        val payload = buildJsonObject {
            put("group_id", groupId)
            put("content", content.trim())
            put("device_hash", deviceHash)
            if (replyToId != null) put("reply_to_id", replyToId)
        }
        supabase.postgrest["group_messages"].insert(payload)
    }

    override suspend fun toggleReaction(
        messageId: String,
        groupId: String,
        emoji: String,
    ) {
        val deviceHash = deviceHashStore.getDeviceHash()

        val body = buildJsonObject {
            put("message_id", messageId)
            put("group_id", groupId)
            put("device_hash", deviceHash)
            put("emoji", emoji)
        }

        val response = supabase.functions.invoke(
            function = "toggle-reaction",
            body     = body,
        )

        val result = json.decodeFromString<ToggleReactionResponse>(response.bodyAsText())
        val reactionsJson = json.encodeToString(
            MapSerializer(
                serializer(),
                ListSerializer(
                   serializer()
                )
            ),
            result.reactions,
        )
        dao.updateReactions(messageId, reactionsJson)
    }

    override suspend fun deleteMessage(groupId: String, messageId: String) {
        try {
            val deviceHash = deviceHashStore.getDeviceHash()
            val updatePayload = buildJsonObject {
                put("is_deleted", true)
                put("deleted_by", deviceHash)
            }
            supabase.postgrest["group_messages"]
                .update(updatePayload) {
                    filter {
                        eq("id", messageId)
                        eq("group_id", groupId)
                    }
                }
            // Realtime UPDATE event will trigger soft delete in Room
        } catch (e: Exception) {
            logger.e(e) { "Error in deleteMessage for messageId: $messageId in group: $groupId" }
            throw e
        }
    }

    override fun subscribeToGroup(groupId: String, adminDeviceHashes: Set<String>) {
        if (adminDeviceHashes.isNotEmpty()) {
            adminHashesByGroup[groupId] = adminDeviceHashes
        }

        if (channelsByGroup[groupId]?.isActive == true) return

        val job = scope.launch {
            try {
                val channel = supabase.channel("group_messages_$groupId")

                val insertFlow = channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
                    table  = "group_messages"
                }

                val updateFlow = channel.postgresChangeFlow<PostgresAction.Update>(schema = "public") {
                    table  = "group_messages"
                }

                // New messages
                insertFlow.onEach { action ->
                    runCatching {
                        val dto = json.decodeFromString<GroupMessageDto>(action.record.toString())

                        logger.d { "Realtime message received: id=${dto.id}, deviceHash=${dto.deviceHash}, group=${dto.groupId}" }

                        if (dto.isDeleted) return@onEach

                        val replyContent = dto.replyToId?.let { replyId ->
                            dao.getById(replyId)?.content
                                ?: fetchReplyPreview(replyId)
                        }

                        val adminHashes = adminHashesByGroup[groupId] ?: emptySet()
                        dao.upsert(
                            dto.toEntity(
                                adminHashes   = adminHashes,
                                replyPreviews = dto.replyToId?.let {
                                    mapOf(it to (replyContent ?: ""))
                                } ?: emptyMap(),
                            )
                        )
                        dao.deleteOlderThan(groupId, cutoffMillis())
                    }
                }.launchIn(this)

                updateFlow.onEach { action ->
                    runCatching {
                        val dto = json.decodeFromString<GroupMessageDto>(action.record.toString())
                        if (dto.isDeleted) dao.softDelete(dto.id) else {
                            val existing = dao.getById(dto.id)
                            if (existing != null) {
                                dao.upsert(
                                    existing.copy(
                                        reactions = dto.reactions,
                                    )
                                )
                            }
                        }
                    }
                }.launchIn(this)

                channel.subscribe()
                logger.d { "Subscribed to realtime messages for group: $groupId" }
            } catch (e: Exception) {
                logger.e(e) { "Error in subscribeToGroup for group: $groupId" }
            }
        }

        channelsByGroup[groupId] = job
    }


    override fun unsubscribeFromGroup(groupId: String) {
        channelsByGroup[groupId]?.cancel()
        channelsByGroup.remove(groupId)
        scope.launch {
            runCatching {
                supabase.realtime.removeChannel(
                    supabase.channel("group_messages_$groupId")
                )
            }.onSuccess {
                logger.d { "Unsubscribed from realtime messages for group: $groupId" }
            }.onFailure { e ->
                logger.e(e) { "Error in unsubscribeFromGroup for group: $groupId" }
            }
        }
    }

    override suspend fun clearLocalMessages(groupId: String) {
        try {
            dao.deleteAllForGroup(groupId)
        } catch (e: Exception) {
            logger.e(e) { "Error in clearLocalMessages for group: $groupId" }
            throw e
        }
    }

    override fun initialize() {
        if (initJob?.isActive == true) return
        initJob = scope.launch {
            supabase.realtime.status.collect { status ->
                if (status == Realtime.Status.CONNECTED) {
                    logger.d { "Realtime connected, initializing group subscriptions" }
                    subscribeToAllGroups()
                }
            }
        }
    }

    private suspend fun subscribeToAllGroups() {
        try {
            val groups = groupRepository.getMyGroups()
            groups.forEach { group ->
                // Fetch admin hashes if we don't have them yet
                if (!adminHashesByGroup.containsKey(group.id)) {
                    runCatching {
                        val members = groupRepository.getMembers(group.id)
                        val adminHashes = members.filter { it.role == GroupMemberRole.ADMIN }
                            .map { it.deviceHash }
                            .toSet()
                        adminHashesByGroup[group.id] = adminHashes
                    }.onFailure { e ->
                        logger.e(e) { "Failed to fetch members for group: ${group.id}" }
                    }
                }
                subscribeToGroup(group.id, adminHashesByGroup[group.id] ?: emptySet())
            }
        } catch (e: Exception) {
            logger.e(e) { "Failed to subscribe to all groups" }
        }
    }

    private fun cutoffMillis(): Long =
        (Clock.System.now() - 72.hours).toEpochMilliseconds()

    private suspend fun fetchReplyPreview(replyId: String): String? =
        runCatching {
            supabase.postgrest["group_messages"]
                .select { filter { eq("id", replyId) } }
                .decodeSingle<ReplyPreviewDto>()
                .content
        }.getOrNull()

}
