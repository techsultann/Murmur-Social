package com.sultlab.murmur.ui.notifications

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class InAppNotification(
    val id: String,
    val title: String,
    val body: String,
    val type: InAppNotificationType,
    val groupId: String? = null,
)

enum class InAppNotificationType {
    GROUP_MESSAGE,
    JOIN_REQUEST,
    MEMBER_JOINED,
    GENERAL,
}

// Singleton — injected via Koin as a single
// Screens that can receive in-app notifications observe the current notification
// and decide whether to show a banner based on which screen the user is on

class InAppNotificationManager(private val scope: CoroutineScope) {

    private val _current = MutableStateFlow<InAppNotification?>(null)
    val current: StateFlow<InAppNotification?> = _current.asStateFlow()

    private var dismissJob: Job? = null

    fun show(notification: InAppNotification) {
        _current.update { notification }

        // Auto-dismiss after 4 seconds
        dismissJob?.cancel()
        dismissJob = scope.launch {
            delay(4000)
            if (_current.value?.id == notification.id) {
                dismiss()
            }
        }
    }

    fun dismiss() {
        _current.update { null }
        dismissJob?.cancel()
    }

    // Convenience helpers

    fun showGroupMessage(groupId: String, groupName: String, preview: String) {
        show(InAppNotification(
            id  = "msg_${System.currentTimeMillis()}",
            title = groupName,
            body = "anonymous: $preview",
            type = InAppNotificationType.GROUP_MESSAGE,
            groupId = groupId,
        ))
    }

    fun showJoinRequest(groupId: String, groupName: String) {
        show(InAppNotification(
            id      = "req_${groupId}",
            title   = "join request",
            body    = "someone wants to join $groupName",
            type    = InAppNotificationType.JOIN_REQUEST,
            groupId = groupId,
        ))
    }

    fun showMemberJoined(groupId: String, groupName: String, count: Int) {
        val body = if (count == 1) "someone joined $groupName"
        else "$count people joined $groupName"
        show(InAppNotification(
            id      = "join_${groupId}",
            title   = "new member",
            body    = body,
            type    = InAppNotificationType.MEMBER_JOINED,
            groupId = groupId,
        ))
    }
}