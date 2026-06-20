package com.sultlab.murmur.ui.group.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultlab.murmur.data.model.Group
import com.sultlab.murmur.data.model.GroupMessage
import com.sultlab.murmur.data.model.GroupVisibility
import com.sultlab.murmur.domain.repository.GroupMessageEvent
import com.sultlab.murmur.domain.use_case.CheckIsGroupAdminUseCase
import com.sultlab.murmur.domain.use_case.DeleteGroupMessageUseCase
import com.sultlab.murmur.domain.use_case.GetCurrentDeviceHashUseCase
import com.sultlab.murmur.domain.use_case.GetGroupMessagesUseCase
import com.sultlab.murmur.domain.use_case.ObserveGroupMessagesUseCase
import com.sultlab.murmur.domain.use_case.SendGroupMessageUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GroupChatViewModel(
    private val initialGroup: Group,
    private val getMessages: GetGroupMessagesUseCase,
    private val getCurrentDeviceHash: GetCurrentDeviceHashUseCase,
    private val sendMessage: SendGroupMessageUseCase,
    private val deleteMessage: DeleteGroupMessageUseCase,
    private val checkIsAdmin: CheckIsGroupAdminUseCase,
    private val observeMessages: ObserveGroupMessagesUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(GroupChatUiState(group = initialGroup))
    val uiState: StateFlow<GroupChatUiState> = _uiState.asStateFlow()

    private var realtimeJob: Job? = null

    init {
        loadMessages()
        checkAdminStatus()
        subscribeToRealtime()
    }

    fun loadMessages() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            runCatching {
                val deviceHash = getCurrentDeviceHash()
                val isPrivate = initialGroup.visibility == GroupVisibility.PRIVATE
                getMessages(initialGroup.id, isPrivate, deviceHash)
            }
                .onSuccess { messages ->
                    _uiState.update { it.copy(messages = messages, isLoading = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun checkAdminStatus() {
        viewModelScope.launch {
            val isAdmin = checkIsAdmin(initialGroup.id)
            _uiState.update { it.copy(isCurrentDeviceAdmin = isAdmin) }
        }
    }

    fun onMessageInputChange(value: String) {
        _uiState.update { it.copy(messageInput = value) }
    }

    fun send() {
        val content = _uiState.value.messageInput
        if (content.isBlank()) return

        _uiState.update { it.copy(isSending = true, error = null) }
        viewModelScope.launch {
            runCatching { sendMessage.execute(initialGroup.id, content) }
                .onSuccess {
                    _uiState.update { it.copy(messageInput = "", isSending = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isSending = false, error = e.message) }
                }
        }
    }

    fun onDeleteMessage(messageId: String) {
        viewModelScope.launch {
            runCatching { deleteMessage.invoke(initialGroup.id, messageId) }
                .onSuccess { loadMessages() }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }

    private fun subscribeToRealtime() {
        realtimeJob?.cancel()
        realtimeJob = viewModelScope.launch {
            observeMessages(initialGroup.id).collect { event ->
                when (event) {
                    is GroupMessageEvent.NewMessage -> {
                        _uiState.update { state ->
                            if (state.messages.any { it.id == event.message.id }) {
                                state
                            } else {
                                state.copy(messages = state.messages + event.message)
                            }
                        }
                    }
                    is GroupMessageEvent.MessageDeleted -> {
                        _uiState.update { state ->
                            state.copy(messages = state.messages.filter { it.id != event.messageId })
                        }
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        realtimeJob?.cancel()
    }
}

data class GroupChatUiState(
    val group: Group,
    val messages: List<GroupMessage> = emptyList(),
    val messageInput: String = "",
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val isCurrentDeviceAdmin: Boolean = false,
    val error: String? = null,
)
