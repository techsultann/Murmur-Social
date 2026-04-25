package com.sultlab.murmur.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class CommentDto(
    val id: String,
    @SerialName("post_id") val postId: String,
    val content: String,
    @SerialName("created_at") val createdAt: Instant,
)