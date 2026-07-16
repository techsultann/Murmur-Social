package com.sultlab.murmur.data.mapper

import com.sultlab.murmur.data.model.Group
import com.sultlab.murmur.data.model.GroupJoinRequest
import com.sultlab.murmur.data.model.GroupMember
import com.sultlab.murmur.data.model.GroupMemberRole
import com.sultlab.murmur.data.model.GroupMemberStatus
import com.sultlab.murmur.data.model.GroupVisibility
import com.sultlab.murmur.data.remote.GroupDto
import com.sultlab.murmur.data.remote.GroupJoinRequestDto
import com.sultlab.murmur.data.remote.GroupMemberDto
import com.sultlab.murmur.data.remote.GroupSummaryDto
import kotlin.time.Clock

fun GroupDto.toDomain(role: GroupMemberRole? = null, status: GroupMemberStatus? = null) = Group(
    id = id,
    joinCode = joinCode,
    name = name,
    description = description,
    visibility = GroupVisibility.valueOf(visibility.uppercase()),
    memberCount = memberCount,
    messageCount = messageCount,
    createdAt = createdAt,
    lastActiveAt = lastActiveAt,
    myRole = role,
    myStatus = status,
)

fun GroupMemberDto.toDomain() = GroupMember(
    id = id,
    groupId = groupId,
    deviceHash = deviceHash,
    role = GroupMemberRole.valueOf(role.uppercase()),
    status = GroupMemberStatus.valueOf(status.uppercase()),
    joinedAt = joinedAt,
)

fun GroupJoinRequestDto.toDomain() = GroupJoinRequest(
    id = id,
    groupId = groupId,
    deviceHash = deviceHash,
    requestedAt = requestedAt,
)

fun GroupSummaryDto.toMinimalGroup() = Group(
    id = id,
    joinCode = joinCode ?: "",
    name = name,
    description  = null,
    visibility = GroupVisibility.valueOf(visibility.uppercase()),
    memberCount  = memberCount,
    messageCount = 0,
    createdAt = Clock.System.now(),
    lastActiveAt = Clock.System.now(),
)