package com.sultlab.murmur.domain.repository

import com.sultlab.murmur.data.model.CreateGroupResult
import com.sultlab.murmur.data.model.Group
import com.sultlab.murmur.data.model.GroupJoinRequest
import com.sultlab.murmur.data.model.GroupMember
import com.sultlab.murmur.data.model.GroupMessage
import com.sultlab.murmur.data.model.GroupVisibility
import com.sultlab.murmur.data.model.JoinGroupResult
import com.sultlab.murmur.data.model.RecoverGroupResult
import kotlinx.coroutines.flow.Flow

sealed interface GroupMessageEvent {
    data class NewMessage(val message: GroupMessage) : GroupMessageEvent
    data class MessageDeleted(val messageId: String) : GroupMessageEvent
}

interface GroupRepository {
    fun observeMyGroups(): Flow<List<Group>>

    suspend fun getMyGroups(): List<Group>

    suspend fun searchPublicGroups(query: String): List<Group>

    suspend fun createGroup(
        name: String,
        description: String?,
        visibility: GroupVisibility,
    ): CreateGroupResult

    suspend fun joinGroup(joinCode: String): JoinGroupResult

    suspend fun recoverGroup(recoveryPhrase: String): RecoverGroupResult

    suspend fun getMembers(groupId: String): List<GroupMember>

    suspend fun getJoinRequests(groupId: String): List<GroupJoinRequest>

    suspend fun getMessages(
        groupId: String,
        isPrivate: Boolean,
        deviceHash: String
    ): List<GroupMessage>

    suspend fun sendMessage(groupId: String, content: String)

    suspend fun removeMember(groupId: String, targetDeviceHash: String)

    suspend fun deleteMessage(groupId: String, messageId: String)

    suspend fun approveJoinRequest(groupId: String, targetDeviceHash: String)

    suspend fun rejectJoinRequest(groupId: String, targetDeviceHash: String)

    suspend fun isCurrentDeviceAdmin(groupId: String): Boolean

    suspend fun currentDeviceHash(): String

    fun observeMessages(groupId: String): Flow<GroupMessageEvent>
}
