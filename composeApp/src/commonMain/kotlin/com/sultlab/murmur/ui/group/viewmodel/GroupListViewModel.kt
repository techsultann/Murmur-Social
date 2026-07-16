package com.sultlab.murmur.ui.group.viewmodel

import com.sultlab.murmur.data.model.Group
import com.sultlab.murmur.data.model.JoinGroupResult
import com.sultlab.murmur.domain.use_case.GetMyGroupsUseCase
import com.sultlab.murmur.domain.use_case.JoinGroupUseCase
import com.sultlab.murmur.domain.use_case.SearchGroupsUseCase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GroupsListViewModel(
    private val getMyGroups: GetMyGroupsUseCase,
    private val searchGroups: SearchGroupsUseCase,
    private val joinGroup: JoinGroupUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(GroupsListUiState())
    val uiState: StateFlow<GroupsListUiState> = _uiState.asStateFlow()

    init {
        loadMyGroups()
    }

    fun loadMyGroups() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            runCatching { getMyGroups() }
                .onSuccess { groups ->
                    _uiState.update { it.copy(myGroups = groups, isLoading = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }

        // If the input looks like a join code, treat search-bar submit as a join attempt
        // (handled explicitly via onSearchSubmit / a paste action in the UI)

        if (query.isBlank()) {
            _uiState.update { it.copy(searchResults = emptyList()) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true) }
            runCatching { searchGroups(query) }
                .onSuccess { results ->
                    _uiState.update { it.copy(searchResults = results, isSearching = false) }
                }
                .onFailure {
                    _uiState.update { it.copy(isSearching = false) }
                }
        }
    }

    // Called when the user pastes/submits something that looks like a join code
    fun attemptJoinByCode(input: String) {
        viewModelScope.launch {
            when (val result = joinGroup.execute(input)) {
                is JoinGroupResult.Joined ->
                    _uiState.update { it.copy(joinedGroup = result.group) }

                is JoinGroupResult.AlreadyMember ->
                    _uiState.update { it.copy(joinedGroup = result.group) }

                is JoinGroupResult.RequestSent ->
                    _uiState.update {
                        it.copy(joinRequestSentGroupName = result.group.name)
                    }

                is JoinGroupResult.Failure ->
                    _uiState.update { it.copy(error = result.message) }
            }
        }
    }

    fun consumeJoinedGroup() = _uiState.update { it.copy(joinedGroup = null) }
    fun consumeJoinRequestToast() = _uiState.update { it.copy(joinRequestSentGroupName = null) }
    fun clearError() = _uiState.update { it.copy(error = null) }
}

data class GroupsListUiState(
    val myGroups: List<Group> = emptyList(),
    val searchQuery: String = "",
    val searchResults: List<Group> = emptyList(),
    val isLoading: Boolean = false,
    val isSearching: Boolean = false,
    val joinedGroup: Group? = null,
    val joinRequestSentGroupName: String? = null,
    val error: String? = null,
)