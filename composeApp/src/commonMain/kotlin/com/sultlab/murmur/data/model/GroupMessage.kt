package com.sultlab.murmur.data.model

import com.sultlab.murmur.data.local.model.GroupMessageEntity
import com.sultlab.murmur.data.remote.GroupMessageDto
import kotlin.time.Instant

fun GroupMessageDto.toEntity(adminHashes: Set<String> = emptySet()) = GroupMessageEntity(
    id        = id,
    groupId   = groupId,
    deviceHash = deviceHash,
    content   = content,
    isAdmin   = deviceHash in adminHashes,
    isDeleted = isDeleted,
    createdAt = Instant.parse(createdAt.toString()).toEpochMilliseconds(),
)

fun GroupMessageEntity.toGroupMessage(adminHashes: Set<String>) = GroupMessage(
    id          = id,
    groupId     = groupId,
    deviceHash  = deviceHash,
    content     = content,
    isFromAdmin = isAdmin,
    createdAt   = Instant.fromEpochMilliseconds(createdAt),
)
