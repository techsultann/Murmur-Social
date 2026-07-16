package com.sultlab.murmur.domain.use_case

import com.sultlab.murmur.data.model.GroupMessage
import com.sultlab.murmur.domain.repository.GroupMessageRepository
import kotlinx.coroutines.flow.Flow


data class GroupMessageUseCases(
    val observeMessages: ObserveGroupMessagesUseCase,
    val loadAndCache: LoadAndCacheMessagesUseCase,
    val sendMessage: SendGroupMessageUseCase,
    val deleteMessage: DeleteGroupMessageUseCase,
    val subscribeToGroup: SubscribeToGroupUseCase,
    val unsubscribeFromGroup: UnsubscribeFromGroupUseCase,
    val clearLocalMessages: ClearLocalMessagesUseCase,
)
class ObserveGroupMessagesUseCase(private val repository: GroupMessageRepository) {
    operator fun invoke(
        groupId: String,
        adminDeviceHashes: Set<String>
    ): Flow<List<GroupMessage>> {
        return repository.observeMessages(groupId, adminDeviceHashes)
    }
}

class LoadAndCacheMessagesUseCase(private val repository: GroupMessageRepository) {
    suspend operator fun invoke(groupId: String) {
        repository.loadAndCache(groupId)
    }
}

class SendGroupMessageUseCase(private val repository: GroupMessageRepository) {
    suspend operator fun invoke(groupId: String, content: String) {
        require(content.isNotBlank()) { "message cannot be empty" }
        require(content.length <= 1000) { "message is too long" }
        repository.sendMessage(groupId, content.trim())
    }
}

class DeleteGroupMessageUseCase(private val repository: GroupMessageRepository) {
    suspend operator fun invoke(groupId: String, messageId: String) {
        repository.deleteMessage(groupId, messageId)
    }
}

class SubscribeToGroupUseCase(private val repository: GroupMessageRepository) {
    operator fun invoke(groupId: String, adminDeviceHashes: Set<String>) {
        repository.subscribeToGroup(groupId, adminDeviceHashes)
    }
}

class UnsubscribeFromGroupUseCase(private val repository: GroupMessageRepository) {
    operator fun invoke(groupId: String) {
        repository.unsubscribeFromGroup(groupId)
    }
}

class ClearLocalMessagesUseCase(private val repository: GroupMessageRepository) {
    suspend operator fun invoke(groupId: String) {
        repository.clearLocalMessages(groupId)
    }
}
