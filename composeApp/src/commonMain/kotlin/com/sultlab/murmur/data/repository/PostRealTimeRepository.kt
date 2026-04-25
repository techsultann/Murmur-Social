package com.sultlab.murmur.data.repository

import com.sultlab.murmur.data.mapper.toDomain
import com.sultlab.murmur.data.model.Comment
import com.sultlab.murmur.data.model.Post
import com.sultlab.murmur.data.remote.CommentRealtimeDto
import com.sultlab.murmur.data.remote.LikeRealtimeDto
import com.sultlab.murmur.data.remote.PostRealtimeDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.time.Instant

class PostRealtimeRepository(
    private val supabase: SupabaseClient,
    private val scope: CoroutineScope,
) {
    sealed interface PostEvent {
        data class Inserted(val post: Post) : PostEvent
        data class Updated(val post: Post) : PostEvent
        data class Removed(val postId: String) : PostEvent
    }

    sealed interface LikeEvent {
        data class Added(val postId: String, val deviceHash: String)   : LikeEvent
        data class Removed(val postId: String, val deviceHash: String) : LikeEvent
    }

    sealed interface CommentEvent {
        data class Added(val comment: Comment) : CommentEvent
    }

    private val _postEvents = MutableSharedFlow<PostEvent>(replay = 0, extraBufferCapacity = 64)
    private val _likeEvents    = MutableSharedFlow<LikeEvent>(replay = 0, extraBufferCapacity = 64)
    private val _commentEvents = MutableSharedFlow<CommentEvent>(replay = 0, extraBufferCapacity = 64)

    val postEvents: SharedFlow<PostEvent> = _postEvents.asSharedFlow()
    val likeEvents: SharedFlow<LikeEvent>    = _likeEvents.asSharedFlow()
    val commentEvents: SharedFlow<CommentEvent> = _commentEvents.asSharedFlow()

    init {
        connect()
    }

    private fun connect() {
        scope.launch {
            val channel = supabase.channel("app_realtime")

            // Posts
            channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
                table = "posts"
            }.onEach { action ->
                runCatching {
                    val dto  = Json.decodeFromString<PostRealtimeDto>(action.record.toString())
                    if (dto.status == "visible") {
                        _postEvents.emit(PostEvent.Inserted(dto.toDomain()))
                    }
                }
            }.launchIn(this)

            channel.postgresChangeFlow<PostgresAction.Update>(schema = "public") {
                table = "posts"
            }.onEach { action ->
                runCatching {
                    val dto = Json.decodeFromString<PostRealtimeDto>(action.record.toString())
                    if (dto.status != "visible") {
                        _postEvents.emit(PostEvent.Removed(dto.id))
                    } else {
                        _postEvents.emit(PostEvent.Updated(dto.toDomain()))
                    }
                }
            }.launchIn(this)

            channel.postgresChangeFlow<PostgresAction.Delete>(schema = "public") {
                table = "posts"
            }.onEach { action ->
                runCatching {
                    val dto = Json.decodeFromString<PostRealtimeDto>(action.oldRecord.toString())
                    _postEvents.emit(PostEvent.Removed(dto.id))
                }
            }.launchIn(this)

            // Likes
            channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
                table = "likes"
            }.onEach { action ->
                runCatching {
                    val dto = Json.decodeFromString<LikeRealtimeDto>(action.record.toString())
                    _likeEvents.emit(LikeEvent.Added(dto.postId, dto.deviceHash))
                }
            }.launchIn(this)

            channel.postgresChangeFlow<PostgresAction.Delete>(schema = "public") {
                table = "likes"
            }.onEach { action ->
                runCatching {
                    val dto = Json.decodeFromString<LikeRealtimeDto>(action.oldRecord.toString())
                    _likeEvents.emit(LikeEvent.Removed(dto.postId, dto.deviceHash))
                }
            }.launchIn(this)

            // Comments
            channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
                table = "comments"
            }.onEach { action ->
                runCatching {
                    val dto = Json.decodeFromString<CommentRealtimeDto>(action.record.toString())
                    if (dto.status == "visible") {
                        val comment = Comment(
                            id        = dto.id,
                            postId    = dto.postId,
                            content   = dto.content,
                            createdAt = Instant.parse(dto.createdAt),
                        )
                        _commentEvents.emit(CommentEvent.Added(comment))
                    }
                }
            }.launchIn(this)

            channel.subscribe()
        }
    }
}