package com.sultlab.murmur.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PostRealtimeDto(
    val id: String,
    val content: String,
    @SerialName("allow_comments") val allowComments: Boolean,
    val status: String,
    @SerialName("like_count") val likeCount: Int,
    @SerialName("comment_count") val commentCount: Int,
    @SerialName("device_hash") val deviceHash: String,
    @SerialName("created_at") val createdAt: String,
)