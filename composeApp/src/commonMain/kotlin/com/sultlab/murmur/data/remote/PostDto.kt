package com.sultlab.murmur.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class PostDto(
    val id: String,
    val content: String,
    @SerialName("allow_comments") val allowComments: Boolean,
    val status: String,
    @SerialName("like_count") val likeCount: Int,
    @SerialName("comment_count") val commentCount: Int,
    @SerialName("created_at") val createdAt: Instant,
)