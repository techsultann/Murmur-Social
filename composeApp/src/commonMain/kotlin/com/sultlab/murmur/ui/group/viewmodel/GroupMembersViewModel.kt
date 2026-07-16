package com.sultlab.murmur.ui.group.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultlab.murmur.data.model.GroupJoinRequest
import com.sultlab.murmur.data.model.GroupMember
import com.sultlab.murmur.domain.use_case.ApproveJoinRequestUseCase
import com.sultlab.murmur.domain.use_case.CheckIsGroupAdminUseCase
import com.sultlab.murmur.domain.use_case.GetGroupMembersUseCase
import com.sultlab.murmur.domain.use_case.GetJoinRequestsUseCase
import com.sultlab.murmur.domain.use_case.RejectJoinRequestUseCase
import com.sultlab.murmur.domain.use_case.RemoveGroupMemberUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GroupMembersViewModel(
    private val groupId: String,
    private val getMembers: GetGroupMembersUseCase,
    private val getJoinRequests: GetJoinRequestsUseCase,
    private val removeMember: RemoveGroupMemberUseCase,
    private val approveJoinRequest: ApproveJoinRequestUseCase,
    private val rejectJoinRequest: RejectJoinRequestUseCase,
    private val checkIsAdmin: CheckIsGroupAdminUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(GroupMembersUiState(groupId = groupId))
    val uiState: StateFlow<GroupMembersUiState> = _uiState.asStateFlow()

    init {
        loadAll()
    }

    fun loadAll() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            runCatching {
                val isAdmin = checkIsAdmin(groupId)
                val members  = getMembers(groupId)
                val requests = if (isAdmin) getJoinRequests(groupId) else emptyList()
                Triple(isAdmin, members, requests)
            }.onSuccess { (isAdmin, members, requests) ->
                _uiState.update {
                    it.copy(
                        isAdmin = isAdmin,
                        members = members,
                        joinRequests = requests,
                        isLoading = false
                    )
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onRemoveMember(targetDeviceHash: String) {
        // Optimistic removal
        _uiState.update { state ->
            state.copy(members = state.members.filter { it.deviceHash != targetDeviceHash })
        }
        viewModelScope.launch {
            runCatching { removeMember(groupId, targetDeviceHash) }
                .onFailure { loadAll() } // re-sync on failure
        }
    }

    fun onApproveRequest(targetDeviceHash: String) {
        val request = _uiState.value.joinRequests.find { it.deviceHash == targetDeviceHash }
        _uiState.update { state ->
            state.copy(joinRequests = state.joinRequests.filter { it.deviceHash != targetDeviceHash })
        }
        viewModelScope.launch {
            runCatching { approveJoinRequest(groupId, targetDeviceHash) }
                .onSuccess { loadAll() } // refresh member list to include the new member
                .onFailure {
                    // restore on failure
                    request?.let { r ->
                        _uiState.update { state -> state.copy(joinRequests = state.joinRequests + r) }
                    }
                }
        }
    }

    fun onRejectRequest(targetDeviceHash: String) {
        val request = _uiState.value.joinRequests.find { it.deviceHash == targetDeviceHash }
        _uiState.update { state ->
            state.copy(joinRequests = state.joinRequests.filter { it.deviceHash != targetDeviceHash })
        }
        viewModelScope.launch {
            runCatching { rejectJoinRequest(groupId, targetDeviceHash) }
                .onFailure {
                    request?.let { r ->
                        _uiState.update { state -> state.copy(joinRequests = state.joinRequests + r) }
                    }
                }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}

data class GroupMembersUiState(
    val groupId: String,
    val isAdmin: Boolean = false,
    val members: List<GroupMember>  = emptyList(),
    val joinRequests: List<GroupJoinRequest> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)
