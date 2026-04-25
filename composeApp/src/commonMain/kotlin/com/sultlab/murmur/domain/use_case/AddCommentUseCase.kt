package com.sultlab.murmur.domain.use_case

import com.sultlab.murmur.data.local.DeviceHashStore
import com.sultlab.murmur.data.model.Comment
import com.sultlab.murmur.domain.repository.CommentRepository

class AddCommentUseCase(
    private val repo: CommentRepository,
    private val deviceHashStore: DeviceHashStore
) {
    suspend operator fun invoke(postId: String, content: String): Comment {
        require(content.isNotBlank()) { "Comment cannot be empty." }
        require(content.length <= 300) { "Comment exceeds 300 characters." }
        return repo.addComment(postId, content.trim(), deviceHashStore.getDeviceHash())
    }
}