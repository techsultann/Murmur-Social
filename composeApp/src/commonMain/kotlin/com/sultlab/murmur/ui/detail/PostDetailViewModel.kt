package com.sultlab.murmur.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Logger.Companion.e
import com.sultlab.murmur.data.model.Comment
import com.sultlab.murmur.data.model.Post
import com.sultlab.murmur.data.model.ReportReason
import com.sultlab.murmur.data.repository.PostRealtimeRepository
import com.sultlab.murmur.domain.use_case.AddCommentUseCase
import com.sultlab.murmur.domain.use_case.GetCommentsUseCase
import com.sultlab.murmur.domain.use_case.LikePostUseCase
import com.sultlab.murmur.domain.use_case.ReportContentUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PostDetailViewModel(
    initialPost: Post,
    private val getComments: GetCommentsUseCase,
    private val addComment: AddCommentUseCase,
    private val likePost: LikePostUseCase,
    private val reportContent: ReportContentUseCase,
    private val realtimeRepo: PostRealtimeRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DetailUiState(post = initialPost))
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        if (initialPost.allowComments) loadComments()
        observeRealtime(initialPost.id)
    }

    private fun loadComments() {
        _uiState.update { it.copy(isLoadingComments = true) }
        viewModelScope.launch {
            runCatching { getComments(_uiState.value.post.id) }
                .onSuccess { comments ->
                    _uiState.update { it.copy(comments = comments, isLoadingComments = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoadingComments = false, error = e.message) }
                }
        }
    }

    fun onCommentInputChange(value: String) {
        _uiState.update { it.copy(commentInput = value) }
    }

    fun sendComment() {
        val state = _uiState.value
        val input = state.commentInput.trim()
        if (input.isBlank() || state.isSendingComment) return

        _uiState.update { it.copy(isSendingComment = true) }
        viewModelScope.launch {
            runCatching { addComment(state.post.id, input) }
                .onSuccess { comment ->
                    _uiState.update {
                        it.copy(
                            comments        = it.comments + comment,
                            commentInput    = "",
                            isSendingComment = false,
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isSendingComment = false, error = e.message) }
                }
        }
    }

    fun toggleLike() {
        val post = _uiState.value.post
        val updatedPost = post.copy(
            likedByMe = !post.likedByMe,
            likeCount = if (post.likedByMe) post.likeCount - 1 else post.likeCount + 1,
        )

        _uiState.update { it.copy(post = updatedPost) }

        viewModelScope.launch {
            runCatching { likePost(post) }
                .onSuccess { Logger.d { "like synced successfully" } }
                .onFailure { e ->
                    Logger.e { "like failed: ${e.message} ${e.cause}" }
                    _uiState.update { it.copy(post = post) }
                }
        }
    }

    fun showReportSheet(commentId: String? = null) {
        _uiState.update { it.copy(reportSheetVisible = true, reportTargetCommentId = commentId) }
    }

    fun hideReportSheet() {
        _uiState.update { it.copy(reportSheetVisible = false, reportTargetCommentId = null) }
    }

    fun submitReport(reason: ReportReason) {
        val state = _uiState.value
        hideReportSheet()
        viewModelScope.launch {
            runCatching {
                val commentId = state.reportTargetCommentId
                if (commentId != null) {
                    reportContent.reportComment(commentId, reason.apiValue)
                } else {
                    reportContent.reportPost(state.post.id, reason.apiValue)
                }
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }


    private fun observeRealtime(postId: String) {
        realtimeRepo.postEvents
            .filterIsInstance<PostRealtimeRepository.PostEvent.Updated>()
            .filter { it.post.id == postId }
            .onEach { event ->
                _uiState.update { state ->
                    state.copy(
                        post = state.post.copy(
                            likeCount    = event.post.likeCount,
                            commentCount = event.post.commentCount,
                            likedByMe    = state.post.likedByMe,  // preserve local
                        )
                    )
                }
            }.launchIn(viewModelScope)

        realtimeRepo.commentEvents
            .filterIsInstance<PostRealtimeRepository.CommentEvent.Added>()
            .filter { it.comment.postId == postId }
            .filter { it.comment.id !in _uiState.value.comments.map { c -> c.id } }
            .onEach { event ->
                _uiState.update { state ->
                    state.copy(comments = state.comments + event.comment)
                }
            }.launchIn(viewModelScope)
    }
}

data class DetailUiState(
    val post: Post,
    val comments: List<Comment> = emptyList(),
    val isLoadingComments: Boolean = false,
    val commentInput: String = "",
    val isSendingComment: Boolean = false,
    val reportSheetVisible: Boolean = false,
    val reportTargetCommentId: String? = null,
    val error: String? = null,
)