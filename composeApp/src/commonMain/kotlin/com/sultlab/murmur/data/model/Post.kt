package com.sultlab.murmur.data.model

import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Post(
    val id: String,
    val content: String,
    val allowComments: Boolean,
    val status: PostStatus,
    val likeCount: Int,
    val commentCount: Int,
    val createdAt: Instant,
    // whether the current device has liked this post
    // resolved client-side against the local likes store
    val likedByMe: Boolean = false,
)

@Serializable
enum class PostStatus { VISIBLE, FLAGGED, REMOVED, PENDING_REVIEW }