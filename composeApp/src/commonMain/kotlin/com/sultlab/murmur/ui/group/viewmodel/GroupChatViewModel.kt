package com.sultlab.murmur.ui.group.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.sultlab.murmur.data.model.Group
import com.sultlab.murmur.data.model.GroupMemberRole
import com.sultlab.murmur.data.remote.GroupMessage
import com.sultlab.murmur.domain.use_case.CheckIsGroupAdminUseCase
import com.sultlab.murmur.domain.use_case.GetCurrentDeviceHashUseCase
import com.sultlab.murmur.domain.use_case.GetGroupMembersUseCase
import com.sultlab.murmur.domain.use_case.GroupMessageUseCases
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GroupChatViewModel(
    private val group: Group,
    private val groupMessage: GroupMessageUseCases,
    private val getMembers: GetGroupMembersUseCase,
    private val checkIsAdmin: CheckIsGroupAdminUseCase,
    private val getCurrentDeviceHash: GetCurrentDeviceHashUseCase,
) : ViewModel() {
    private val logger = Logger.withTag("GroupChatViewModel")
    private val _uiState = MutableStateFlow(GroupChatUiState(group = group))
    val uiState: StateFlow<GroupChatUiState> = _uiState.asStateFlow()

    private var adminDeviceHashes = emptySet<String>()

    init {
        resolveAdminHashesAndLoad()
    }

    // ── Resolve who the admins are first, then subscribe + load ──
    // Admin hashes are needed to correctly tag messages as isFromAdmin.

    private fun resolveAdminHashesAndLoad() {
        viewModelScope.launch {
            // 1. Fetch member list to know which device hashes are admins
            runCatching { getMembers(group.id) }
                .onSuccess { members ->
                    adminDeviceHashes = members
                        .filter { it.role == GroupMemberRole.ADMIN }
                        .map { it.deviceHash }
                        .toSet()
                }

            // 2. Check if current device is admin and get its hash
            val isAdmin = runCatching { checkIsAdmin(group.id) }.getOrDefault(false)
            val deviceHash = runCatching { getCurrentDeviceHash() }.getOrDefault("")
            _uiState.update { it.copy(isCurrentDeviceAdmin = isAdmin, currentDeviceHash = deviceHash) }

            // 3. Subscribe to Realtime (writes incoming events to Room)
            groupMessage.subscribeToGroup(group.id, adminDeviceHashes)

            // 4. Observe Room — this Flow drives the UI from now on
            groupMessage
                .observeMessages(group.id, adminDeviceHashes)
                .onEach { messages ->
                    logger.d { "Observed ${messages.size} messages for group ${group.id}" }
                    messages.forEach { msg ->
                        logger.d { "Message: id=${msg.id}, deviceHash=${msg}, content=${msg.content.take(20)}..." }
                    }
                    _uiState.update { it.copy(messages = messages, isLoading = false) }
                }
                .launchIn(viewModelScope)

            // 5. Fetch + cache latest 72h messages from Supabase into Room
            runCatching { groupMessage.loadAndCache(group.id) }
                .onSuccess {
                    logger.d { "Successfully loaded and cached messages for group ${group.id}" }
                }
                .onFailure { e ->
                    logger.e(e) { "Failed to load and cache messages for group ${group.id}" }
                    // Room already has local data, so just log the error —
                    // don't show it to the user unless Room is also empty
                    if (_uiState.value.messages.isEmpty()) {
                        _uiState.update { it.copy(isLoading = false, error = e.message) }
                    }
                }
        }
    }

    fun onMessageInputChange(value: String) {
        _uiState.update { it.copy(messageInput = value) }
    }

    fun onReply(message: GroupMessage) {
        _uiState.update { it.copy(replyingTo = message) }
    }

    fun clearReply() {
        _uiState.update { it.copy(replyingTo = null) }
    }

    fun send() {
        val state = _uiState.value
        val content = state.messageInput.trim()
        if (content.isBlank() || _uiState.value.isSending) return

        logger.d { "Sending message to group ${group.id}" }
        _uiState.update { it.copy(isSending = true) }

        viewModelScope.launch {
            runCatching {
                groupMessage.sendMessage(group.id, content, replyToId = state.replyingTo?.id)
            }
                .onSuccess {
                    logger.d { "Successfully sent message to group ${group.id}" }
                    _uiState.update { it.copy(messageInput = "", replyingTo = null, isSending = false) }
                    // Realtime delivers the new message → Room → UI
                }
                .onFailure { e ->
                    logger.e(e) { "Failed to send message to group ${group.id}" }
                    _uiState.update { it.copy(isSending = false, error = e.message) }
                }
        }
    }

    fun toggleReaction(messageId: String, emoji: String) {
        viewModelScope.launch {
            runCatching {
                groupMessage.toggleReaction(
                    messageId = messageId,
                    groupId   = group.id,
                    emoji     = emoji,
                )
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun deleteMessage(messageId: String) {
        logger.d { "Deleting message $messageId from group ${group.id}" }
        viewModelScope.launch {
            runCatching { groupMessage.deleteMessage(group.id, messageId) }
                .onSuccess {
                    logger.d { "Successfully deleted message $messageId" }
                }
                .onFailure { e ->
                    logger.e(e) { "Failed to delete message $messageId" }
                    _uiState.update { it.copy(error = e.message) }
                }
            // Realtime UPDATE event → Room soft delete → UI removes it
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }

    override fun onCleared() {
        groupMessage.unsubscribeFromGroup(group.id)
    }
}

data class GroupChatUiState(
    val group: Group,
    val messages: List<GroupMessage> = emptyList(),
    val replyingTo: GroupMessage? = null,
    val messageInput: String = "",
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val isCurrentDeviceAdmin: Boolean = false,
    val currentDeviceHash: String = "",
    val error: String? = null,
)
