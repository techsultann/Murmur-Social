package com.sultlab.murmur.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultlab.murmur.data.model.Post
import com.sultlab.murmur.data.repository.PostRealtimeRepository
import com.sultlab.murmur.domain.use_case.GetFeedUseCase
import com.sultlab.murmur.domain.use_case.LikePostUseCase
import io.github.jan.supabase.realtime.Realtime
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FeedViewModel(
    private val getFeed: GetFeedUseCase,
    private val likePost: LikePostUseCase,
    private val realtimeRepo: PostRealtimeRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    private val likedPostIds = mutableSetOf<String>()

    private var realtimeJob: Job? = null
    private val channelName = "feed_realtime"

    init {
        loadFeed()
        observeRealtime()
    }

    fun loadFeed() {
        if (_uiState.value.isLoading) return
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            runCatching { getFeed() }
                .onSuccess { posts ->
                    _uiState.update {
                        it.copy(
                            posts        = posts,
                            isLoading    = false,
                            hasReachedEnd = posts.size < 20,
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, error = e.message)
                    }
                }
        }
    }

    private fun observeRealtime() {
        // New posts
        realtimeRepo.postEvents
            .filterIsInstance<PostRealtimeRepository.PostEvent.Inserted>()
            .onEach { event ->
                _uiState.update { state ->
                    val already = state.posts.any { it.id == event.post.id }
                    if (already) state
                    else state.copy(posts = listOf(event.post) + state.posts)
                }
            }.launchIn(viewModelScope)

        // Post updates — restore likedByMe from local set
        realtimeRepo.postEvents
            .filterIsInstance<PostRealtimeRepository.PostEvent.Updated>()
            .onEach { event ->
                _uiState.update { state ->
                    state.copy(
                        posts = state.posts.map { p ->
                            if (p.id == event.post.id) event.post.copy(
                                likedByMe = likedPostIds.contains(event.post.id)
                            ) else p
                        }
                    )
                }
            }.launchIn(viewModelScope)

        // Removals
        realtimeRepo.postEvents
            .filterIsInstance<PostRealtimeRepository.PostEvent.Removed>()
            .onEach { event ->
                _uiState.update { state ->
                    state.copy(posts = state.posts.filter { it.id != event.postId })
                }
            }.launchIn(viewModelScope)
    }

    fun loadMore() {
        val state = _uiState.value
        if (state.isLoadingMore || state.hasReachedEnd || state.posts.isEmpty()) return

        _uiState.update { it.copy(isLoadingMore = true) }
        viewModelScope.launch {
            val oldest = state.posts.last().createdAt
            runCatching { getFeed(before = oldest) }
                .onSuccess { more ->
                    _uiState.update {
                        it.copy(
                            posts         = it.posts + more,
                            isLoadingMore = false,
                            hasReachedEnd = more.size < 20,
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoadingMore = false, error = e.message)
                    }
                }
        }
    }

    fun incrementCommentCount(postId: String) {
        _uiState.update { state ->
            state.copy(
                posts = state.posts.map { p ->
                    if (p.id == postId) p.copy(commentCount = p.commentCount + 1) else p
                }
            )
        }
    }

    fun toggleLike(post: Post) {
        val updatedPost = post.copy(
            likedByMe = !post.likedByMe,
            likeCount = if (post.likedByMe) post.likeCount - 1 else post.likeCount + 1,
        )

        // Optimistic UI update
        _uiState.update { state ->
            state.copy(
                posts = state.posts.map { p ->
                    if (p.id == post.id) updatedPost else p
                }
            )
        }

        viewModelScope.launch {
            runCatching { likePost(post) }  // ← original post, not updatedPost
                .onFailure {
                    _uiState.update { state ->
                        state.copy(
                            posts = state.posts.map { p ->
                                if (p.id == post.id) post else p
                            }
                        )
                    }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class FeedUiState(
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val hasReachedEnd: Boolean = false,
)
