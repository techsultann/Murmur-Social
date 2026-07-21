package com.sultlab.murmur.data.repository

import com.sultlab.murmur.data.local.DeviceHashStore
import com.sultlab.murmur.data.local.dao.GroupDao
import com.sultlab.murmur.data.mapper.toDomain
import com.sultlab.murmur.data.mapper.toEntity
import com.sultlab.murmur.data.mapper.toMinimalGroup
import com.sultlab.murmur.data.model.CreateGroupResult
import com.sultlab.murmur.data.model.Group
import com.sultlab.murmur.data.model.GroupJoinRequest
import com.sultlab.murmur.data.model.GroupMember
import com.sultlab.murmur.data.model.GroupMemberRole
import com.sultlab.murmur.data.model.GroupMemberStatus
import com.sultlab.murmur.data.model.GroupVisibility
import com.sultlab.murmur.data.model.JoinGroupResult
import com.sultlab.murmur.data.model.RecoverGroupResult
import com.sultlab.murmur.data.remote.CreateGroupResponse
import com.sultlab.murmur.data.remote.GroupDto
import com.sultlab.murmur.data.remote.GroupJoinRequestDto
import com.sultlab.murmur.data.remote.GroupMemberDto
import com.sultlab.murmur.data.remote.GroupMessageDto
import com.sultlab.murmur.data.remote.GroupMessagesResponse
import com.sultlab.murmur.data.remote.GroupSummaryDto
import com.sultlab.murmur.data.remote.JoinGroupResponse
import com.sultlab.murmur.data.remote.RecoverGroupResponse
import com.sultlab.murmur.data.remote.GroupMessageRealtimeDto
import com.sultlab.murmur.domain.repository.GroupMessageEvent
import com.sultlab.murmur.domain.repository.GroupRepository
import co.touchlab.kermit.Logger
import com.sultlab.murmur.data.remote.GroupMessage
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.postgrest.query.filter.FilterOperation
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.time.Clock
import kotlin.time.Instant

class GroupRepositoryImpl(
    private val client: SupabaseClient,
    private val deviceHashStore: DeviceHashStore,
    private val groupDao: GroupDao,
) : GroupRepository {

    private val logger = Logger.withTag("GroupRepository")
    private val json = Json { ignoreUnknownKeys = true }

    override fun observeMyGroups(): Flow<List<Group>> =
        groupDao.observeAll().map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun getMyGroups(): List<Group> {
        return try {
            val deviceHash = deviceHashStore.getDeviceHash()
            val memberships = client.postgrest["group_members"]
                .select {
                    filter {
                        eq("device_hash", deviceHash)
                        eq("status", "active")
                    }
                }
                .decodeList<GroupMemberDto>()

            if (memberships.isEmpty()) return emptyList()

            val groupIds = memberships.map { it.groupId }
            val roleByGroupId = memberships.associate { it.groupId to it.role }

            val groups = client.postgrest["groups"]
                .select {
                    filter { isIn("id", groupIds) }
                    order("last_active_at", Order.DESCENDING)
                }
                .decodeList<GroupDto>()

            val domainGroups = groups.map { dto ->
                dto.toDomain(
                    role = roleByGroupId[dto.id]?.let { GroupMemberRole.valueOf(it.uppercase()) },
                    status = GroupMemberStatus.ACTIVE,
                )
            }

            groupDao.deleteAll()
            groupDao.upsertAll(domainGroups.map { it.toEntity() })

            domainGroups
        } catch (e: Exception) {
            logger.e(e) { "Error in getMyGroups" }
            throw e
        }
    }

    override suspend fun searchPublicGroups(query: String): List<Group> = try {
        client.postgrest["groups"]
            .select {
                filter {
                    eq("visibility", "public")
                    ilike("name", "%$query%")
                }
                order("member_count", Order.DESCENDING)
                limit(20)
            }
            .decodeList<GroupDto>()
            .map { it.toDomain() }
    } catch (e: Exception) {
        logger.e(e) { "Error searching groups with query: $query" }
        emptyList()
    }

    // ── Create / Join / Recover (via edge functions) ─────────

    override suspend fun createGroup(
        name: String,
        description: String?,
        visibility: GroupVisibility,
    ): CreateGroupResult = try {
        val deviceHash = deviceHashStore.getDeviceHash()
        val body = buildJsonObject {
            put("name", name)
            description?.let { put("description", it) }
            put("visibility", visibility.name.lowercase())
            put("device_hash", deviceHash)
        }

        val response = client.functions.invoke(function = "create_group", body = body)
        val result = json.decodeFromString<CreateGroupResponse>(response.bodyAsText())

        if (result.group != null && result.recoveryPhrase != null) {
            val group = Group(
                id = result.group.id,
                joinCode = result.group.joinCode ?: "",
                name = result.group.name,
                description = description,
                visibility = GroupVisibility.valueOf(result.group.visibility.uppercase()),
                memberCount = 1,
                messageCount = 0,
                createdAt = Clock.System.now(),
                lastActiveAt = Clock.System.now(),
                myRole = GroupMemberRole.ADMIN,
                myStatus = GroupMemberStatus.ACTIVE,
            )
            groupDao.upsert(group.toEntity())
            CreateGroupResult.Success(
                group = group,
                recoveryPhrase = result.recoveryPhrase,
            )
        } else {
            CreateGroupResult.Failure(result.error ?: "failed to create group")
        }
    } catch (e: Exception) {
        logger.e(e) { "Error creating group: $name" }
        CreateGroupResult.Failure(e.message ?: "failed to create group")
    }

    override suspend fun joinGroup(joinCode: String): JoinGroupResult = try {
        val deviceHash = deviceHashStore.getDeviceHash()
        val body = buildJsonObject {
            put("join_code", joinCode)
            put("device_hash", deviceHash)
        }
        Logger.d { "join-group body: $body" }
        val response = client.functions.invoke(function = "join_group", body = body)
        val result = json.decodeFromString<JoinGroupResponse>(response.bodyAsText())

        val groupSummary = result.group
        when {
            result.error != null -> JoinGroupResult.Failure(result.error)
            groupSummary == null -> JoinGroupResult.Failure("group not found")
            result.status == "joined" -> {
                val group = groupSummary.toMinimalGroup()
                groupDao.upsert(group.toEntity())
                JoinGroupResult.Joined(group)
            }
            result.status == "already_member" -> {
                val group = groupSummary.toMinimalGroup()
                groupDao.upsert(group.toEntity())
                JoinGroupResult.AlreadyMember(group)
            }
            result.status == "request_sent" || result.status == "request_pending" ->
                JoinGroupResult.RequestSent(groupSummary.toMinimalGroup())
            else -> JoinGroupResult.Failure("unexpected response")
        }
    } catch (e: Exception) {
        logger.e(e) { "Error joining group: $joinCode" }
        JoinGroupResult.Failure(e.message ?: "failed to join group")
    }

    override suspend fun recoverGroup(recoveryPhrase: String): RecoverGroupResult = try {
        val deviceHash = deviceHashStore.getDeviceHash()
        val body = buildJsonObject {
            put("recovery_phrase", recoveryPhrase)
            put("device_hash", deviceHash)
        }

        val response = client.functions.invoke(function = "recover_group", body = body)
        val result = json.decodeFromString<RecoverGroupResponse>(response.bodyAsText())

        if (result.group != null) {
            val group = result.group.toMinimalGroup()
            groupDao.upsert(group.toEntity())
            RecoverGroupResult.Success(group)
        } else {
            RecoverGroupResult.Failure(result.error ?: "recovery failed")
        }
    } catch (e: Exception) {
        logger.e(e) { "Error recovering group" }
        RecoverGroupResult.Failure(e.message ?: "recovery failed")
    }

    // ── Members ────────────────────────────────────────────────

    override suspend fun getMembers(groupId: String): List<GroupMember> = try {
        client.postgrest["group_members"]
            .select {
                filter {
                    eq("group_id", groupId)
                    eq("status", "active")
                }
                order("joined_at", Order.ASCENDING)
            }
            .decodeList<GroupMemberDto>()
            .map { it.toDomain() }
    } catch (e: Exception) {
        logger.e(e) { "Error fetching members for group: $groupId" }
        emptyList()
    }

    override suspend fun getJoinRequests(groupId: String): List<GroupJoinRequest> = try {
        client.postgrest["group_join_requests"]
            .select {
                filter { eq("group_id", groupId) }
                order("requested_at", Order.ASCENDING)
            }
            .decodeList<GroupJoinRequestDto>()
            .map { it.toDomain() }
    } catch (e: Exception) {
        logger.e(e) { "Error fetching join requests for group: $groupId" }
        emptyList()
    }

    // ── Messages ───────────────────────────────────────────────

    override suspend fun getMessages(
        groupId: String,
        isPrivate: Boolean,
        deviceHash: String
    ): List<GroupMessage> = try {
        val members = getMembers(groupId)
        val adminHashes = members.filter { it.role == GroupMemberRole.ADMIN }.map { it.deviceHash }.toSet()
        val currentDeviceHash = deviceHashStore.getDeviceHash()
        val messagesDto = if (isPrivate) {
            val body = buildJsonObject {
                put("group_id", groupId)
                put("device_hash", deviceHash)
            }
            val response = client.functions.invoke(function = "get_group_messages", body = body)
            json.decodeFromString<GroupMessagesResponse>(response.bodyAsText()).messages
        } else {
            client.postgrest["group_messages"]
                .select {
                    filter {
                        eq("group_id", groupId)
                        eq("is_deleted", false)
                    }
                    order("created_at", Order.ASCENDING)
                }
                .decodeList<GroupMessageDto>()
        }

        messagesDto.map {
            GroupMessage(
                id = it.id,
                groupId = it.groupId,
                deviceHash = it.deviceHash,
                content = it.content,
                isFromAdmin = it.deviceHash in adminHashes,
                createdAt = Instant.parse(it.createdAt),
                isOwnMessage = it.deviceHash == currentDeviceHash
            )
        }
    } catch (e: Exception) {
        logger.e(e) { "Error fetching messages for group: $groupId" }
        emptyList()
    }

    override suspend fun sendMessage(groupId: String, content: String) {
        try {
            val deviceHash = deviceHashStore.getDeviceHash()
            val payload = buildJsonObject {
                put("group_id", groupId)
                put("content", content)
                put("device_hash", deviceHash)
            }
            client.postgrest["group_messages"].insert(payload)
        } catch (e: Exception) {
            logger.e(e) { "Error sending message to group: $groupId" }
        }
    }


    override suspend fun removeMember(groupId: String, targetDeviceHash: String) {
        invokeAdminAction(
            action = "remove_member",
            groupId = groupId,
            adminDeviceHash = deviceHashStore.getDeviceHash(),
            targetDeviceHash = targetDeviceHash,
        )
    }

    override suspend fun deleteMessage(groupId: String, messageId: String) {
        invokeAdminAction(
            action  = "delete_message",
            groupId = groupId,
            adminDeviceHash = deviceHashStore.getDeviceHash(),
            messageId = messageId,
        )
    }

    override suspend fun approveJoinRequest(groupId: String, targetDeviceHash: String) {
        invokeAdminAction(
            action = "approve_request",
            groupId = groupId,
            adminDeviceHash = deviceHashStore.getDeviceHash(),
            targetDeviceHash = targetDeviceHash,
        )
    }

    override suspend fun rejectJoinRequest(groupId: String, targetDeviceHash: String) {
        invokeAdminAction(
            action = "reject_request",
            groupId = groupId,
            adminDeviceHash = deviceHashStore.getDeviceHash(),
            targetDeviceHash = targetDeviceHash,
        )
    }

    override suspend fun isCurrentDeviceAdmin(groupId: String): Boolean = try {
        val hash = deviceHashStore.getDeviceHash()
        getMembers(groupId).any {
            it.deviceHash == hash && it.role == GroupMemberRole.ADMIN
        }
    } catch (e: Exception) {
        logger.e(e) { "Error checking admin status for group: $groupId" }
        false
    }

    override suspend fun currentDeviceHash(): String = try {
        deviceHashStore.getDeviceHash()
    } catch (e: Exception) {
        logger.e(e) { "Error getting current device hash" }
        ""
    }

    override fun observeMessages(groupId: String): Flow<GroupMessageEvent> = callbackFlow {
        logger.d { "Observing messages for group: $groupId" }

        // Resolve admin hashes once for this subscription
        val members = getMembers(groupId)
        val adminHashes = members.filter { it.role == GroupMemberRole.ADMIN }
            .map { it.deviceHash }
            .toSet()

        // Use a unique channel name to avoid IllegalStateException: "You cannot call postgresChangeFlow after joining the channel"
        // This happens if multiple collectors use the same channel name simultaneously.
        val channelId = "group_chat_${groupId}_${Clock.System.now().toEpochMilliseconds()}"
        val channel = client.channel(channelId)

        // Subscribe to ALL changes for group_messages and filter on the client side.
        // This is more robust as some Supabase Realtime versions have issues with server-side filters on certain columns or events.
        channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "group_messages"
        }.onEach { action ->
            runCatching {
                val record = when (action) {
                    is PostgresAction.Insert -> action.record
                    is PostgresAction.Update -> action.record
                    else -> return@onEach
                }

                val dto = json.decodeFromString<GroupMessageRealtimeDto>(record.toString())
                if (dto.groupId != groupId) return@onEach

                when (action) {
                    is PostgresAction.Insert -> {
                        if (!dto.isDeleted) {
                            val message = GroupMessage(
                                id = dto.id,
                                groupId = dto.groupId,
                                deviceHash = dto.deviceHash,
                                content = dto.content,
                                isFromAdmin = dto.deviceHash in adminHashes,
                                createdAt = Instant.parse(dto.createdAt),
                                isOwnMessage = dto.deviceHash == currentDeviceHash()
                            )
                            trySend(GroupMessageEvent.NewMessage(message))
                        }
                    }
                    is PostgresAction.Update -> {
                        if (dto.isDeleted) {
                            trySend(GroupMessageEvent.MessageDeleted(dto.id))
                        }
                    }
                }
            }.onFailure {
                logger.e(it) { "Error processing realtime message for group: $groupId, action: $action" }
            }
        }.launchIn(this)

        try {
            channel.subscribe()
            logger.d { "Successfully joined channel: $channelId" }
        } catch (e: Exception) {
            logger.e(e) { "Error subscribing to channel: $channelId for group: $groupId" }
            close(e)
        }

        awaitClose {
            logger.d { "Stopping observation for group: $groupId" }
            launch(NonCancellable) {
                runCatching { client.realtime.removeChannel(channel) }
            }
        }
    }

    private suspend fun invokeAdminAction(
        action: String,
        groupId: String,
        adminDeviceHash: String,
        targetDeviceHash: String? = null,
        messageId: String? = null,
    ) {
        try {
            val body = buildJsonObject {
                put("action", action)
                put("group_id", groupId)
                put("admin_device_hash", adminDeviceHash)
                targetDeviceHash?.let { put("target_device_hash", it) }
                messageId?.let { put("message_id", it) }
            }
            client.functions.invoke(function = "group_admin_actions", body = body)
        } catch (e: Exception) {
            logger.e(e) { "Error invoking admin action: $action for group: $groupId" }
        }
    }

    override suspend fun muteGroup(groupId: String) {
        val deviceHash = deviceHashStore.getDeviceHash()
        client.postgrest["notification_mutes"].insert(
            mapOf("device_hash" to deviceHash, "group_id" to groupId)
        )
    }

    override suspend fun unmuteGroup(groupId: String) {
        val deviceHash = deviceHashStore.getDeviceHash()
        client.postgrest["notification_mutes"].delete {
            filter {
                eq("device_hash", deviceHash)
                eq("group_id", groupId)
            }
        }
    }

    override suspend fun isGroupMuted(groupId: String): Boolean {
        val deviceHash = deviceHashStore.getDeviceHash()
        val result = client.postgrest["notification_mutes"]
            .select { filter { eq("device_hash", deviceHash); eq("group_id", groupId) } }
            .decodeList<Map<String, String>>()
        return result.isNotEmpty()
    }

    override suspend fun getGroupById(groupId: String): Group? {
        return groupDao.getGroupById(groupId)?.toDomain()
    }

    override suspend fun getRecoveryPhrase(groupId: String): String? {
        val deviceHash = deviceHashStore.getDeviceHash()
        val body = buildJsonObject {
            put("group_id", groupId)
            put("device_hash", deviceHash)
        }
        return runCatching {
            val response = client.functions.invoke(
                function = "get-recovery-phrase",
                body     = body,
            )
            val result = Json.decodeFromString<Map<String, String>>(response.bodyAsText())
            result["recovery_phrase"]
        }.getOrNull()
    }

}
