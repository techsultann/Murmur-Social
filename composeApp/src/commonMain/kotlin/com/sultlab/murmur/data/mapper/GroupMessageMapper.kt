package com.sultlab.murmur.data.mapper

import com.sultlab.murmur.data.local.model.GroupMessageEntity
import com.sultlab.murmur.data.remote.GroupMessage
import com.sultlab.murmur.data.remote.GroupMessageDto
import kotlin.time.Instant

fun GroupMessageDto.toEntity(
    adminHashes: Set<String> = emptySet(),
    replyPreviews: Map<String, String> = emptyMap(),
) = GroupMessageEntity(
    id = id,
    groupId = groupId,
    deviceHash = deviceHash,
    content = content,
    isAdmin = deviceHash in adminHashes,
    replyToId = replyToId,
    replyToContent = replyToId?.let { replyPreviews[it] },
    isDeleted = isDeleted,
    reactions = reactions,
    createdAt = Instant.parse(createdAt).toEpochMilliseconds(),
)

fun GroupMessageEntity.toGroupMessage(
    adminHashes: Set<String> = emptySet(),
    currentDeviceHash: String = "",
) = GroupMessage(
    id               = id,
    groupId          = groupId,
    deviceHash       = deviceHash,
    content          = content,
    isFromAdmin      = isAdmin,
    isOwnMessage     = deviceHash == currentDeviceHash,
    replyToId        = replyToId,
    replyToContent   = replyToContent,
    reactions        = reactions,
    createdAt        = Instant.fromEpochMilliseconds(createdAt),
)

private fun GroupMessageEntity.toDomain(currentDeviceHash: String) = GroupMessage(
    id = id,
    groupId = groupId,
    content = content,
    deviceHash = deviceHash,
    isFromAdmin = isAdmin,
    isOwnMessage = deviceHash == currentDeviceHash,
    replyToId = replyToId,
    replyToContent = replyToContent,
    reactions = reactions,
    createdAt = Instant.fromEpochMilliseconds(createdAt),
)

