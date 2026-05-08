package com.sultlab.murmur.data.repository

import co.touchlab.kermit.Logger
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
    private val logger = Logger.withTag("PostRealtimeRepository")
    private val json = Json { ignoreUnknownKeys = true }

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
            logger.d { "Connecting to real-time channel..." }
            val channel = supabase.channel("app_realtime")

            // Posts
            channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
                table = "posts"
            }.onEach { action ->
                runCatching {
                    val dto  = json.decodeFromString<PostRealtimeDto>(action.record.toString())
                    if (dto.status == "visible") {
                        logger.d { "Post Inserted: ${dto.id}" }
                        _postEvents.emit(PostEvent.Inserted(dto.toDomain()))
                    }
                }.onFailure {
                    logger.e(it) { "Error processing Post Inserted: ${action.record}" }
                }
            }.launchIn(this)

            channel.postgresChangeFlow<PostgresAction.Update>(schema = "public") {
                table = "posts"
            }.onEach { action ->
                runCatching {
                    val dto = json.decodeFromString<PostRealtimeDto>(action.record.toString())
                    if (dto.status != "visible") {
                        logger.d { "Post Removed (status change): ${dto.id}" }
                        _postEvents.emit(PostEvent.Removed(dto.id))
                    } else {
                        logger.d { "Post Updated: ${dto.id}" }
                        _postEvents.emit(PostEvent.Updated(dto.toDomain()))
                    }
                }.onFailure {
                    logger.e(it) { "Error processing Post Updated: ${action.record}" }
                }
            }.launchIn(this)

            channel.postgresChangeFlow<PostgresAction.Delete>(schema = "public") {
                table = "posts"
            }.onEach { action ->
                runCatching {
                    val dto = json.decodeFromString<PostRealtimeDto>(action.oldRecord.toString())
                    logger.d { "Post Deleted: ${dto.id}" }
                    _postEvents.emit(PostEvent.Removed(dto.id))
                }.onFailure {
                    logger.e(it) { "Error processing Post Deleted: ${action.oldRecord}" }
                }
            }.launchIn(this)

            // Likes
            channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
                table = "likes"
            }.onEach { action ->
                runCatching {
                    val dto = json.decodeFromString<LikeRealtimeDto>(action.record.toString())
                    logger.d { "Like Added: post=${dto.postId}, hash=${dto.deviceHash}" }
                    _likeEvents.emit(LikeEvent.Added(dto.postId, dto.deviceHash))
                }.onFailure {
                    logger.e(it) { "Error processing Like Added: ${action.record}" }
                }
            }.launchIn(this)

            channel.postgresChangeFlow<PostgresAction.Delete>(schema = "public") {
                table = "likes"
            }.onEach { action ->
                runCatching {
                    val dto = json.decodeFromString<LikeRealtimeDto>(action.oldRecord.toString())
                    logger.d { "Like Removed: post=${dto.postId}, hash=${dto.deviceHash}" }
                    _likeEvents.emit(LikeEvent.Removed(dto.postId, dto.deviceHash))
                }.onFailure {
                    logger.e(it) { "Error processing Like Removed: ${action.oldRecord}" }
                }
            }.launchIn(this)

            // Comments
            channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
                table = "comments"
            }.onEach { action ->
                runCatching {
                    val dto = json.decodeFromString<CommentRealtimeDto>(action.record.toString())
                    if (dto.status == "visible") {
                        logger.d { "Comment Added: post=${dto.postId}, comment=${dto.id}" }
                        val comment = Comment(
                            id        = dto.id,
                            postId    = dto.postId,
                            content   = dto.content,
                            createdAt = Instant.parse(dto.createdAt),
                        )
                        _commentEvents.emit(CommentEvent.Added(comment))
                    }
                }.onFailure {
                    logger.e(it) { "Error processing Comment Added: ${action.record}" }
                }
            }.launchIn(this)

            runCatching {
                channel.subscribe()
                logger.d { "Subscribed to channel" }
            }.onFailure {
                logger.e(it) { "Failed to subscribe to channel" }
            }
        }
    }
}
