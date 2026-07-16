package com.sultlab.murmur.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GroupMessageRealtimeDto(
    val id: String,
    @SerialName("group_id") val groupId: String,
    val content: String,
    @SerialName("device_hash") val deviceHash: String,
    @SerialName("is_deleted") val isDeleted: Boolean,
    @SerialName("deleted_by") val deletedBy: String? = null,
    @SerialName("created_at") val createdAt: String,
)
