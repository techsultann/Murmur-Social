package com.sultlab.murmur.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultlab.murmur.data.model.Post
import com.sultlab.murmur.domain.use_case.GetPostByIdUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PostDetailByIdViewModel(
    private val postId: String,
    private val getPostById: GetPostByIdUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow<PostDetailByIdUiState>(PostDetailByIdUiState.Loading)
    val uiState: StateFlow<PostDetailByIdUiState> = _uiState.asStateFlow()

    init {
        loadPost()
    }

    private fun loadPost() {
        viewModelScope.launch {
            getPostById(postId)
                .onSuccess { post ->
                    _uiState.update { PostDetailByIdUiState.Success(post) }
                }
                .onFailure { e ->
                    _uiState.update { PostDetailByIdUiState.Error(e.message ?: "Failed to load post") }
                }
        }
    }
}

sealed interface PostDetailByIdUiState {
    data object Loading : PostDetailByIdUiState
    data class Success(val post: Post) : PostDetailByIdUiState
    data class Error(val message: String) : PostDetailByIdUiState
}
