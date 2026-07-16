package com.sultlab.murmur.data.repository

import co.touchlab.kermit.Logger
import com.sultlab.murmur.data.local.DeviceHashStore
import com.sultlab.murmur.data.local.dao.GroupMessageDao
import com.sultlab.murmur.data.model.GroupMessage
import com.sultlab.murmur.data.model.toEntity
import com.sultlab.murmur.data.model.toGroupMessage
import com.sultlab.murmur.data.remote.GroupMessageDto
import com.sultlab.murmur.domain.repository.GroupMessageRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

class GroupMessageRepositoryImpl(
    private val supabase: SupabaseClient,
    private val dao: GroupMessageDao,
    private val deviceHashStore: DeviceHashStore,
    private val scope: CoroutineScope,
) : GroupMessageRepository {

    private val logger = Logger.withTag("GroupMessageRepository")
    private val channelsByGroup = mutableMapOf<String, Job>()
    private val json = Json { ignoreUnknownKeys = true }

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

            // Enforce local 72h cutoff before caching
            val cutoff = cutoffMillis()
            dao.deleteOlderThan(groupId, cutoff)

            // Upsert everything from server into Room
            dao.upsertAll(dtos.map { it.toEntity() })
        } catch (e: Exception) {
            logger.e(e) { "Error in loadAndCache for group: $groupId" }
            throw e
        }
    }

    // ── Send a message ────────────────────────────────────────

    override suspend fun sendMessage(groupId: String, content: String) {
        try {
            val deviceHash = deviceHashStore.getDeviceHash()
            supabase.postgrest["group_messages"].insert(
                mapOf(
                    "group_id" to groupId,
                    "content" to content.trim(),
                    "device_hash" to deviceHash,
                )
            )
            // Realtime will deliver the message back to all clients including sender,
            // which then writes it to Room — no optimistic insert needed here.
        } catch (e: Exception) {
            logger.e(e) { "Error in sendMessage for group: $groupId" }
            throw e
        }
    }

    // ── Admin: delete a message ───────────────────────────────

    override suspend fun deleteMessage(groupId: String, messageId: String) {
        try {
            val deviceHash = deviceHashStore.getDeviceHash()
            supabase.postgrest["group_messages"]
                .update(mapOf("is_deleted" to true, "deleted_by" to deviceHash)) {
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

    // ── Subscribe to Realtime for a group ─────────────────────

    override fun subscribeToGroup(groupId: String, adminDeviceHashes: Set<String>) {
        if (channelsByGroup.containsKey(groupId)) return  // already subscribed

        val job = scope.launch {
            try {
                val channel = supabase.channel("group_messages_$groupId")

                // New messages
                channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
                    table = "group_messages"
                }.onEach { action ->
                    runCatching {
                        val dto = json.decodeFromString<GroupMessageDto>(
                            action.record.toString()
                        )
                        if (dto.isDeleted) return@onEach

                        // Write to Room — Room Flow notifies UI
                        dao.upsert(dto.toEntity())

                        // Enforce 72h locally after every new message
                        dao.deleteOlderThan(groupId, cutoffMillis())
                    }.onFailure { e ->
                        logger.e(e) { "Error processing Insert realtime action for group: $groupId" }
                    }
                }.launchIn(this)

                // Message deleted by admin — soft delete in Room
                channel.postgresChangeFlow<PostgresAction.Update>(schema = "public") {
                    table = "group_messages"
                }.onEach { action ->
                    runCatching {
                        val dto = json.decodeFromString<GroupMessageDto>(
                            action.record.toString()
                        )
                        if (dto.isDeleted) {
                            dao.softDelete(dto.id)
                        }
                    }.onFailure { e ->
                        logger.e(e) { "Error processing Update realtime action for group: $groupId" }
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

    // ── Unsubscribe when leaving the group chat ───────────────

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

    private fun cutoffMillis(): Long =
        (Clock.System.now() - 72.hours).toEpochMilliseconds()

}
