package com.sultlab.murmur.domain.use_case

import com.sultlab.murmur.data.model.CreateGroupResult
import com.sultlab.murmur.data.model.Group
import com.sultlab.murmur.data.model.GroupJoinRequest
import com.sultlab.murmur.data.model.GroupMember
import com.sultlab.murmur.data.model.GroupVisibility
import com.sultlab.murmur.data.model.JoinGroupResult
import com.sultlab.murmur.data.model.RecoverGroupResult
import com.sultlab.murmur.domain.repository.GroupMessageEvent
import com.sultlab.murmur.domain.repository.GroupRepository
import kotlinx.coroutines.flow.Flow

class ObserveMyGroupsUseCase(private val repo: GroupRepository) {
    operator fun invoke(): Flow<List<Group>> = repo.observeMyGroups()
}

class GetMyGroupsUseCase(private val repo: GroupRepository) {
    suspend operator fun invoke(): List<Group> = repo.getMyGroups()
}

class SearchGroupsUseCase(private val repo: GroupRepository) {
    suspend operator fun invoke(query: String): List<Group> =
        if (query.isBlank()) emptyList() else repo.searchPublicGroups(query.trim())
}

class CreateGroupUseCase(private val repo: GroupRepository) {
    suspend fun execute(
        name: String,
        description: String?,
        visibility: GroupVisibility,
    ): CreateGroupResult {
        if (name.isBlank()) return CreateGroupResult.Failure("group name cannot be empty")
        if (name.length > 60) return CreateGroupResult.Failure("group name is too long")
        return repo.createGroup(name.trim(), description?.trim()?.ifBlank { null }, visibility)
    }
}

class JoinGroupUseCase(private val repo: GroupRepository) {
    suspend fun execute(joinCodeOrInput: String): JoinGroupResult {
        // Accept either the raw code (X4f8-bK2-a9c) or a pasted share string
        val code = extractJoinCode(joinCodeOrInput)
            ?: return JoinGroupResult.Failure("that doesn't look like a valid join code")
        return repo.joinGroup(code)
    }

    private fun extractJoinCode(input: String): String? {
        val pattern = Regex("""[A-Za-z0-9]{4}-[A-Za-z0-9]{3}-[A-Za-z0-9]{3}""")
        return pattern.find(input.trim())?.value
    }
}

class RecoverGroupUseCase(private val repo: GroupRepository) {
    suspend fun execute(phrase: String): RecoverGroupResult {
        val normalized = phrase.trim().lowercase().replace(Regex("\\s+"), " ")
        val wordCount = normalized.split(" ").filter { it.isNotBlank() }.size
        if (wordCount != 12) {
            return RecoverGroupResult.Failure("recovery phrase must contain exactly 12 words")
        }
        return repo.recoverGroup(normalized)
    }
}

class GetGroupMembersUseCase(private val repo: GroupRepository) {
    suspend operator fun invoke(groupId: String): List<GroupMember> = repo.getMembers(groupId)
}

class GetJoinRequestsUseCase(private val repo: GroupRepository) {
    suspend operator fun invoke(groupId: String): List<GroupJoinRequest> = repo.getJoinRequests(groupId)
}


class RemoveGroupMemberUseCase(private val repo: GroupRepository) {
    suspend operator fun invoke(groupId: String, targetDeviceHash: String) =
        repo.removeMember(groupId, targetDeviceHash)
}


class ApproveJoinRequestUseCase(private val repo: GroupRepository) {
    suspend operator fun invoke(groupId: String, targetDeviceHash: String) =
        repo.approveJoinRequest(groupId, targetDeviceHash)
}

class RejectJoinRequestUseCase(private val repo: GroupRepository) {
    suspend operator fun invoke(groupId: String, targetDeviceHash: String) =
        repo.rejectJoinRequest(groupId, targetDeviceHash)
}

class CheckIsGroupAdminUseCase(private val repo: GroupRepository) {
    suspend operator fun invoke(groupId: String): Boolean = repo.isCurrentDeviceAdmin(groupId)
}

class GetCurrentDeviceHashUseCase(private val repo: GroupRepository) {
    suspend operator fun invoke(): String = repo.currentDeviceHash()
}

