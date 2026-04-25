package com.sultlab.murmur.domain.use_case

import com.sultlab.murmur.data.local.DeviceHashStore
import com.sultlab.murmur.data.model.ReportReason
import com.sultlab.murmur.domain.repository.CommentRepository
import com.sultlab.murmur.domain.repository.PostRepository

class ReportContentUseCase(
    private val postRepo: PostRepository,
    private val commentRepo: CommentRepository,
    private val deviceHashStore: DeviceHashStore,
) {
    suspend fun reportPost(postId: String, reason: String) =
        postRepo.reportPost(
            postId = postId,
            deviceHash = deviceHashStore.getDeviceHash(),
            reason = reason
        )

    suspend fun reportComment(commentId: String, reason: String) =
        commentRepo.reportComment(
            commentId = commentId,
            deviceHash = deviceHashStore.getDeviceHash(),
            reason = reason
        )
}