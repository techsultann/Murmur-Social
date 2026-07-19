package com.sultlab.murmur.ui.group.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultlab.murmur.data.model.Group
import com.sultlab.murmur.data.model.GroupJoinRequest
import com.sultlab.murmur.domain.repository.GroupRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class GroupAdminViewModel(
    private val group: Group,
    private val groupRepo: GroupRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(GroupAdminUiState(group = group))
    val uiState: StateFlow<GroupAdminUiState> = _uiState.asStateFlow()

    init {
        loadJoinRequests()
        loadMemberCount()
    }

    private fun loadJoinRequests() {
        _uiState.update { it.copy(isLoadingRequests = true) }
        viewModelScope.launch {
            runCatching { groupRepo.getJoinRequests(group.id) }
                .onSuccess { requests ->
                    _uiState.update { it.copy(joinRequests = requests, isLoadingRequests = false) }
                }
                .onFailure {
                    _uiState.update { it.copy(isLoadingRequests = false) }
                }
        }
    }

    private fun loadMemberCount() {
        viewModelScope.launch {
            runCatching { groupRepo.getMembers(group.id) }
                .onSuccess { members ->
                    _uiState.update { it.copy(memberCount = members.size) }
                }
        }
    }


    fun togglePhraseVisibility() {
        val state = _uiState.value
        if (state.recoveryPhrase != null) {
            // Already loaded — just toggle visibility
            _uiState.update { it.copy(phraseVisible = !it.phraseVisible) }
            return
        }

        // Fetch the phrase
        _uiState.update { it.copy(isLoadingPhrase = true) }
        viewModelScope.launch {
            runCatching { groupRepo.getRecoveryPhrase(group.id) }
                .onSuccess { phrase ->
                    _uiState.update {
                        it.copy(
                            recoveryPhrase  = phrase,
                            phraseVisible   = true,
                            isLoadingPhrase = false,
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoadingPhrase = false,
                            error           = e.message ?: "could not load recovery phrase",
                        )
                    }
                }
        }
    }

    fun onPhraseCopied() {
        _uiState.update { it.copy(copiedPhrase = true) }
        viewModelScope.launch {
            kotlinx.coroutines.delay(2000)
            _uiState.update { it.copy(copiedPhrase = false) }
        }
    }

    fun onJoinCodeCopied() {
        _uiState.update { it.copy(copiedJoinCode = true) }
        viewModelScope.launch {
            kotlinx.coroutines.delay(2000)
            _uiState.update { it.copy(copiedJoinCode = false) }
        }
    }

    // ── Join requests ─────────────────────────────────────────

    fun approveRequest(deviceHash: String) {
        _uiState.update { state ->
            state.copy(joinRequests = state.joinRequests.filter { it.deviceHash != deviceHash })
        }
        viewModelScope.launch {
            runCatching { groupRepo.approveJoinRequest(group.id, deviceHash) }
                .onFailure { loadJoinRequests() }
        }
    }

    fun rejectRequest(deviceHash: String) {
        _uiState.update { state ->
            state.copy(joinRequests = state.joinRequests.filter { it.deviceHash != deviceHash })
        }
        viewModelScope.launch {
            runCatching { groupRepo.rejectJoinRequest(group.id, deviceHash) }
                .onFailure { loadJoinRequests() }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}


data class GroupAdminUiState(
    val group: Group,
    val recoveryPhrase: String?          = null,
    val isLoadingPhrase: Boolean         = false,
    val phraseVisible: Boolean           = false,
    val joinRequests: List<GroupJoinRequest> = emptyList(),
    val isLoadingRequests: Boolean       = false,
    val memberCount: Int                 = 0,
    val copiedPhrase: Boolean            = false,
    val copiedJoinCode: Boolean          = false,
    val error: String?                   = null,
)