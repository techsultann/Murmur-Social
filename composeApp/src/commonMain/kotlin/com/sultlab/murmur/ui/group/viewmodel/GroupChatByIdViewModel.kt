package com.sultlab.murmur.ui.group.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultlab.murmur.data.model.Group
import com.sultlab.murmur.domain.use_case.GetGroupByIdUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GroupChatByIdViewModel(
    private val groupId: String,
    private val getGroupById: GetGroupByIdUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow<GroupChatByIdUiState>(GroupChatByIdUiState.Loading)
    val uiState: StateFlow<GroupChatByIdUiState> = _uiState.asStateFlow()

    init {
        loadGroup()
    }

    private fun loadGroup() {
        viewModelScope.launch {
            val group = getGroupById(groupId)
            if (group != null) {
                _uiState.update { GroupChatByIdUiState.Success(group) }
            } else {
                _uiState.update { GroupChatByIdUiState.Error("Group not found") }
            }
        }
    }
}

sealed interface GroupChatByIdUiState {
    data object Loading : GroupChatByIdUiState
    data class Success(val group: Group) : GroupChatByIdUiState
    data class Error(val message: String) : GroupChatByIdUiState
}
