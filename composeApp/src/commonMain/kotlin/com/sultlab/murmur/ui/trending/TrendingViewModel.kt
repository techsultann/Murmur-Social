package com.sultlab.murmur.ui.trending

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultlab.murmur.data.mapper.toDomain
import com.sultlab.murmur.data.model.Post
import com.sultlab.murmur.data.remote.PostDto
import com.sultlab.murmur.domain.use_case.LikePostUseCase
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

class TrendingViewModel(
    private val supabase: SupabaseClient,
    private val likePost: LikePostUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TrendingUiState())
    val uiState: StateFlow<TrendingUiState> = _uiState.asStateFlow()

    init { load() }

    fun setWindow(window: TrendingWindow) {
        if (_uiState.value.window == window) return
        _uiState.update { it.copy(window = window) }
        load()
    }

    fun load() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            runCatching {
                val since = (Clock.System.now() - _uiState.value.window.hours.hours).toString()

                // Fetch visible posts from the trending window, ordered by like_count desc.
                // A proper trending score (e.g. Wilson score or time-decay) would be computed
                // server-side as a Postgres function; this simple approach works for v1.
                supabase.postgrest["posts"]
                    .select {
                        filter {
                            eq("status", "visible")
                            gte("created_at", since)
                        }
                        order("like_count", Order.DESCENDING)
                        limit(20)
                    }
                    .decodeList<PostDto>()
                    .map { it.toDomain() }
            }
                .onSuccess { posts ->
                    _uiState.update {
                        it.copy(
                            posts     = posts,
                            maxLikes  = posts.maxOfOrNull { p -> p.likeCount } ?: 1,
                            isLoading = false,
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun toggleLike(post: Post) {
        _uiState.update { state ->
            state.copy(
                posts = state.posts.map { p ->
                    if (p.id == post.id) p.copy(
                        likedByMe = !p.likedByMe,
                        likeCount = if (p.likedByMe) p.likeCount - 1 else p.likeCount + 1,
                    ) else p
                }
            )
        }
        viewModelScope.launch {
            runCatching { likePost(post) }
                .onFailure {
                    _uiState.update { state ->
                        state.copy(posts = state.posts.map { p ->
                            if (p.id == post.id) post else p
                        })
                    }
                }
        }
    }
}

enum class TrendingWindow(val label: String, val hours: Int) {
    DAY("24h", 24),
    WEEK("7d", 168),
}

data class TrendingUiState(
    val posts: List<Post> = emptyList(),
    val window: TrendingWindow = TrendingWindow.DAY,
    val isLoading: Boolean = false,
    val error: String? = null,
    val maxLikes: Int = 1,
)