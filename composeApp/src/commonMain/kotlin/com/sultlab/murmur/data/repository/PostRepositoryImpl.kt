package com.sultlab.murmur.data.repository

import co.touchlab.kermit.Logger
import com.sultlab.murmur.data.local.DeviceHashStore
import com.sultlab.murmur.data.local.LikesStore
import com.sultlab.murmur.data.mapper.toDomain
import com.sultlab.murmur.data.model.Post
import com.sultlab.murmur.data.remote.ModerateResult
import com.sultlab.murmur.data.remote.PostDto
import com.sultlab.murmur.domain.repository.PostRepository
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.time.Instant

class PostRepositoryImpl(
    private val postgrest: Postgrest,
    private val functions: Functions,
    private val likesStore: LikesStore
) : PostRepository {

    override suspend fun getFeed(
        limit: Int,
        beforeCreatedAt: Instant?,
    ): List<Post> {

        val query = if (beforeCreatedAt == null) {
            postgrest["posts"]
                .select {
                    filter { eq("status", "visible") }
                    order("created_at", Order.DESCENDING)
                    limit(limit.toLong())
                }
        } else {
            postgrest["posts"]
                .select {
                    filter {
                        eq("status", "visible")
                        lt("created_at", beforeCreatedAt.toString())
                    }
                    order("created_at", Order.DESCENDING)
                    limit(limit.toLong())
                }
        }

        val likedIds = likesStore.getLikedPostIds().toHashSet()

        return query.decodeList<PostDto>()
            .map { dto ->
                val post = dto.toDomain()
                post.copy(
                    likedByMe = post.id in likedIds
                )
            }
    }

    // Calls the moderate-post edge function first;
    // only inserts if allowed == true.
    override suspend fun createPost(
        content: String,
        deviceHash: String,
        allowComments: Boolean,
    ): Result<Post> = runCatching {

        // 1. Pre-moderation
        val moderateBody = buildJsonObject {
            put("content",     content)
            put("device_hash", deviceHash)
        }

        val moderateResponse = functions.invoke(
            function = "moderate_post",
            body = moderateBody,
        )

        val result = Json.decodeFromString<ModerateResult>(moderateResponse.bodyAsText())

        if (!result.allowed) {
            val message = when (result.reason) {
                "device_banned" -> "Your device has been banned."
                "rate_limited"  -> "You're posting too quickly. Try again in a bit."
                "toxic_content" -> "Your post was flagged for review."
                else -> "Post could not be submitted."
            }
            error(message)
        }

        // 2. Insert
        val postBody = buildJsonObject {
            put("content", content)
            put("device_hash", deviceHash)
            put("allow_comments", allowComments)
        }

        val inserted = postgrest["posts"]
            .insert(postBody) { select() }
            .decodeSingle<PostDto>()

        inserted.toDomain()
    }

    override suspend fun likePost(postId: String, deviceHash: String) {
        val likeBody = buildJsonObject {
            put("post_id", postId)
            put("device_hash", deviceHash)
        }
        postgrest["likes"].insert(likeBody)
    }

    override suspend fun unlikePost(postId: String, deviceHash: String) {
        postgrest["likes"].delete {
            filter {
                eq("post_id",    postId)
                eq("device_hash", deviceHash)
            }
        }
    }

    override suspend fun toggleLike(post: Post, deviceHash: String) {
        if (post.likedByMe) {
            unlikePost(post.id, deviceHash)
            likesStore.removeLike(post.id)
            Logger(config = Logger.config, tag = "PostRepository").d { "unliked post ${post.id}" }
        } else {
            likePost(post.id, deviceHash)
            likesStore.addLike(post.id)
            Logger(config = Logger.config, tag = "PostRepository").d { "liked post ${post.id}" }
        }
    }

    override suspend fun reportPost(
        postId: String,
        deviceHash: String,
        reason: String,
    ) {
        val reportBody = buildJsonObject {
            put("post_id", postId)
            put("device_hash", deviceHash)
            put("reason", reason)
        }
        postgrest["reports"].insert(reportBody)
    }
}
