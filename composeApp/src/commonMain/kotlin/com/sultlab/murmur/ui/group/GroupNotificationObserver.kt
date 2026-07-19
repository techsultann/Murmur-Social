package com.sultlab.murmur.ui.group

import com.sultlab.murmur.domain.repository.GroupRepository
import com.sultlab.murmur.ui.notifications.InAppNotificationManager
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// The currently visible group chat — set by navigation.
// When this matches the incoming event's groupId, we suppress the banner.

class GroupNotificationObserver(
    private val supabase: SupabaseClient,
    private val groupRepo: GroupRepository,
    private val notificationManager: InAppNotificationManager,
    private val currentDeviceHash: String,
    private val scope: CoroutineScope,
) {
    private val json = Json { ignoreUnknownKeys = true }

    fun start() {
        scope.launch {
            val myGroups = runCatching { groupRepo.getMyGroups() }.getOrDefault(emptyList())
            if (myGroups.isEmpty()) return@launch

            val groupNamesById = myGroups.associate { it.id to it.name }
            val myGroupIds = myGroups.map { it.id }

            val channel = supabase.channel("group_notifications_observer")

            // ── New group messages ────────────────────────────────
            channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
                table = "group_messages"
            }.onEach { action ->
                runCatching {
                    val event = json.decodeFromString<GroupMessageEvent>(
                        action.record.toString()
                    )
                    // Ignore if not in my groups, deleted, or sent by me
                    if (event.groupId !in myGroupIds) return@onEach
                    if (event.isDeleted) return@onEach
                    if (event.deviceHash == currentDeviceHash) return@onEach
                    // Suppress if user is currently viewing this group's chat
                    if (ActiveGroupScreen.currentGroupId.value == event.groupId) return@onEach

                    val groupName = groupNamesById[event.groupId] ?: "a group"
                    val preview   = event.content.take(60).let {
                        if (event.content.length > 60) "$it…" else it
                    }
                    notificationManager.showGroupMessage(event.groupId, groupName, preview)
                }
            }.launchIn(this)

            // ── Join requests (admin only) ────────────────────────
            channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
                table = "group_join_requests"
            }.onEach { action ->
                runCatching {
                    val event = json.decodeFromString<JoinRequestEvent>(
                        action.record.toString()
                    )
                    if (event.groupId !in myGroupIds) return@onEach
                    if (event.deviceHash == currentDeviceHash) return@onEach

                    // Only show banner if this device is an admin
                    val group  = myGroups.find { it.id == event.groupId } ?: return@onEach
                    val isAdmin = group.myRole?.name == "ADMIN"
                    if (!isAdmin) return@onEach

                    notificationManager.showJoinRequest(event.groupId, group.name)
                }
            }.launchIn(this)

            // ── New members (admin only) ──────────────────────────
            channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
                table = "group_members"
            }.onEach { action ->
                runCatching {
                    val event = json.decodeFromString<GroupMemberEvent>(
                        action.record.toString()
                    )
                    if (event.groupId !in myGroupIds) return@onEach
                    if (event.deviceHash == currentDeviceHash) return@onEach
                    if (event.status != "active") return@onEach
                    if (event.role == "admin") return@onEach

                    val group   = myGroups.find { it.id == event.groupId } ?: return@onEach
                    val isAdmin = group.myRole?.name == "ADMIN"
                    if (!isAdmin) return@onEach

                    notificationManager.showMemberJoined(event.groupId, group.name, 1)
                }
            }.launchIn(this)

            channel.subscribe()
        }
    }
}

object ActiveGroupScreen {
    val currentGroupId = MutableStateFlow<String?>(null)
}

@Serializable
private data class GroupMessageEvent(
    @SerialName("group_id")    val groupId: String,
    val content: String,
    @SerialName("device_hash") val deviceHash: String,
    @SerialName("is_deleted")  val isDeleted: Boolean = false,
)

@Serializable
private data class JoinRequestEvent(
    @SerialName("group_id")    val groupId: String,
    @SerialName("device_hash") val deviceHash: String,
)

@Serializable
private data class GroupMemberEvent(
    @SerialName("group_id")    val groupId: String,
    @SerialName("device_hash") val deviceHash: String,
    val role: String,
    val status: String,
)