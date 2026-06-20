package com.sultlab.murmur.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class GroupMessageDto(
    val id: String,
    @SerialName("group_id") val groupId: String,
    val content: String,
    @SerialName("device_hash") val deviceHash: String,
    @SerialName("is_deleted") val isDeleted: Boolean,
    @SerialName("created_at") val createdAt: Instant,
)

@Serializable
data class GroupMessagesResponse(
    val messages: List<GroupMessageDto> = emptyList(),
    val error: String? = null
)
