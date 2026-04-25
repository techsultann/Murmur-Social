package com.sultlab.murmur.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LikeRealtimeDto(
    val id: String,
    @SerialName("post_id") val postId: String,
    @SerialName("device_hash") val deviceHash: String,
)