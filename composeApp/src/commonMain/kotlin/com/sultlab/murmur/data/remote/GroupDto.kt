package com.sultlab.murmur.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class GroupDto(
    val id: String,
    @SerialName("join_code") val joinCode: String,
    val name: String,
    val description: String? = null,
    val visibility: String,
    @SerialName("member_count") val memberCount: Int,
    @SerialName("message_count") val messageCount: Int,
    @SerialName("created_at") val createdAt: Instant,
    @SerialName("last_active_at") val lastActiveAt: Instant,
)