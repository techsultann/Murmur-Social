package com.sultlab.murmur.ui.group.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultlab.murmur.data.model.Group
import com.sultlab.murmur.data.model.RecoverGroupResult
import com.sultlab.murmur.domain.use_case.RecoverGroupUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RecoverGroupViewModel(
    private val recoverGroup: RecoverGroupUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RecoverGroupUiState())
    val uiState: StateFlow<RecoverGroupUiState> = _uiState.asStateFlow()

    fun onPhraseChange(value: String) =
        _uiState.update { it.copy(phraseInput = value, error = null) }

    fun submit() {
        val phrase = _uiState.value.phraseInput
        if (phrase.isBlank()) return

        _uiState.update { it.copy(isRecovering = true, error = null) }

        viewModelScope.launch {
            when (val result = recoverGroup.execute(phrase)) {
                is RecoverGroupResult.Success ->
                    _uiState.update { it.copy(isRecovering = false, recoveredGroup = result.group) }
                is RecoverGroupResult.Failure ->
                    _uiState.update { it.copy(isRecovering = false, error = result.message) }
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}

data class RecoverGroupUiState(
    val phraseInput: String = "",
    val isRecovering: Boolean = false,
    val recoveredGroup: Group? = null,
    val error: String? = null,
)