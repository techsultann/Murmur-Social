package com.sultlab.murmur.data.model

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class Group(
    val id: String,
    val joinCode: String,
    val name: String,
    val description: String?,
    val visibility: GroupVisibility,
    val memberCount: Int,
    val messageCount: Int,
    val createdAt: Instant,
    val lastActiveAt: Instant,
    // resolved client-side based on current device's membership row
    val myRole: GroupMemberRole? = null,
    val myStatus: GroupMemberStatus? = null,
)

@Serializable
enum class GroupVisibility { PUBLIC, PRIVATE }
@Serializable
enum class GroupMemberRole { ADMIN, MEMBER }
@Serializable
enum class GroupMemberStatus { ACTIVE, PENDING, REMOVED }

data class GroupMember(
    val id: String,
    val groupId: String,
    val deviceHash: String,   // never displayed raw in UI — used only for actions
    val role: GroupMemberRole,
    val status: GroupMemberStatus,
    val joinedAt: Instant,
)

data class GroupJoinRequest(
    val id: String,
    val groupId: String,
    val deviceHash: String,
    val requestedAt: Instant,
)

// ── Result types for group operations ───────────────────────

sealed interface CreateGroupResult {
    data class Success(val group: Group, val recoveryPhrase: String) : CreateGroupResult
    data class Failure(val message: String) : CreateGroupResult
}

sealed interface JoinGroupResult {
    data class Joined(val group: Group) : JoinGroupResult
    data class RequestSent(val group: Group) : JoinGroupResult
    data class AlreadyMember(val group: Group) : JoinGroupResult
    data class Failure(val message: String) : JoinGroupResult
}

sealed interface RecoverGroupResult {
    data class Success(val group: Group) : RecoverGroupResult
    data class Failure(val message: String) : RecoverGroupResult
}