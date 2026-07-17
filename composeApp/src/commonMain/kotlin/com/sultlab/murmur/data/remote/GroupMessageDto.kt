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
    @SerialName("deleted_by") val deletedBy: String? = null,
    @SerialName("reply_to_id") val replyToId: String? = null,
    val reactions: Map<String, List<String>> = emptyMap(),
    @SerialName("created_at") val createdAt: String,
)

@Serializable
data class GroupMessagesResponse(
    val messages: List<GroupMessageDto> = emptyList(),
    val error: String? = null
)
