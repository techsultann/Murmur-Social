package com.sultlab.murmur.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class GroupJoinRequestDto(
    val id: String,
    @SerialName("group_id") val groupId: String,
    @SerialName("device_hash")  val deviceHash: String,
    @SerialName("requested_at") val requestedAt: Instant,
)