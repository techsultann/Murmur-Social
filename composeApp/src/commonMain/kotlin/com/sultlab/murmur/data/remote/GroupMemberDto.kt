package com.sultlab.murmur.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class GroupMemberDto(
    val id: String,
    @SerialName("group_id") val groupId: String,
    @SerialName("device_hash") val deviceHash: String,
    val role: String,
    val status: String,
    @SerialName("joined_at") val joinedAt: Instant,
)