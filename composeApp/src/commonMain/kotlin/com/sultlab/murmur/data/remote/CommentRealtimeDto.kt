package com.sultlab.murmur.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CommentRealtimeDto(
    val id: String,
    @SerialName("post_id") val postId: String,
    val content: String,
    val status: String,
    @SerialName("device_hash") val deviceHash: String,
    @SerialName("created_at") val createdAt: String,
)