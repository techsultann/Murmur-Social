package com.sultlab.murmur.ui.compose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultlab.murmur.data.remote.CreatePostResult
import com.sultlab.murmur.domain.use_case.CreatePostUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ComposePostViewModel(
    private val createPost: CreatePostUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ComposeUiState())
    val uiState: StateFlow<ComposeUiState> = _uiState.asStateFlow()

    fun onContentChange(value: String) {
        _uiState.update { it.copy(content = value, error = null) }
    }

    fun onAllowCommentsToggle(value: Boolean) {
        _uiState.update { it.copy(allowComments = value) }
    }

    fun submit() {
        val state = _uiState.value
        if (!state.canSubmit) return

        _uiState.update { it.copy(isSubmitting = true, error = null) }

        viewModelScope.launch {
            val result = createPost.execute(state.content, state.allowComments)
            when (result) {
                is CreatePostResult.Success ->
                    _uiState.update { it.copy(isSubmitting = false, submitted = true) }
                is CreatePostResult.Failure ->
                    _uiState.update { it.copy(isSubmitting = false, error = result.message) }
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }

}

data class ComposeUiState(
    val content: String = "",
    val allowComments: Boolean = true,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val submitted: Boolean = false,
) {
    val charCount: Int get() = content.length
    val isOverLimit: Boolean get() = charCount > 500
    val canSubmit: Boolean get() = content.isNotBlank() && !isOverLimit && !isSubmitting
}