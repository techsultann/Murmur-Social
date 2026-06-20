package com.sultlab.murmur.ui.group.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultlab.murmur.data.model.CreateGroupResult
import com.sultlab.murmur.data.model.Group
import com.sultlab.murmur.data.model.GroupVisibility
import com.sultlab.murmur.domain.use_case.CreateGroupUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreateGroupViewModel(
    private val createGroup: CreateGroupUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CreateGroupUiState())
    val uiState: StateFlow<CreateGroupUiState> = _uiState.asStateFlow()

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value, error = null) }
    fun onDescriptionChange(value: String) = _uiState.update { it.copy(description = value) }
    fun onVisibilityChange(value: GroupVisibility) = _uiState.update { it.copy(visibility = value) }

    fun submit() {
        val state = _uiState.value
        if (!state.canSubmit) return

        _uiState.update { it.copy(isCreating = true, error = null) }

        viewModelScope.launch {
            when (val result = createGroup.execute(state.name, state.description, state.visibility)) {
                is CreateGroupResult.Success ->
                    _uiState.update {
                        it.copy(
                            isCreating = false,
                            createdGroup = result.group,
                            recoveryPhrase  = result.recoveryPhrase,
                        )
                    }
                is CreateGroupResult.Failure ->
                    _uiState.update { it.copy(isCreating = false, error = result.message) }
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}

data class CreateGroupUiState(
    val name: String = "",
    val description: String = "",
    val visibility: GroupVisibility = GroupVisibility.PUBLIC,
    val isCreating: Boolean = false,
    val error: String? = null,
    // Set on success — the UI navigates to the recovery phrase screen with this
    val createdGroup: Group? = null,
    val recoveryPhrase: String? = null,
) {
    val canSubmit: Boolean get() = name.isNotBlank() && name.length <= 60 && !isCreating
}