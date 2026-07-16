package com.sultlab.murmur.data.model

import com.sultlab.murmur.data.local.model.GroupMessageEntity
import com.sultlab.murmur.data.remote.GroupMessageDto
import kotlin.time.Instant

fun GroupMessageDto.toEntity() = GroupMessageEntity(
    id        = id,
    groupId   = groupId,
    content   = content,
    isAdmin   = false,   // admin status resolved separately from group_members
    isDeleted = isDeleted,
    createdAt = Instant.parse(createdAt.toString()).toEpochMilliseconds(),
)

fun GroupMessageEntity.toGroupMessage(adminHashes: Set<String>) = GroupMessage(
    id          = id,
    groupId     = groupId,
    content     = content,
    isFromAdmin = isAdmin,
    createdAt   = Instant.fromEpochMilliseconds(createdAt),
)