package com.sultlab.murmur.domain.repository

import com.sultlab.murmur.data.remote.GroupMessage
import kotlinx.coroutines.flow.Flow

interface GroupMessageRepository {
    fun observeMessages(groupId: String, adminDeviceHashes: Set<String>): Flow<List<GroupMessage>>
    suspend fun loadAndCache(groupId: String)
    suspend fun sendMessage(groupId: String, content: String, replyToId: String? = null)

    suspend fun toggleReaction(messageId: String, groupId: String, emoji: String)
    suspend fun deleteMessage(groupId: String, messageId: String)
    fun subscribeToGroup(groupId: String, adminDeviceHashes: Set<String>)
    fun unsubscribeFromGroup(groupId: String)
    suspend fun clearLocalMessages(groupId: String)
    fun initialize()
}
