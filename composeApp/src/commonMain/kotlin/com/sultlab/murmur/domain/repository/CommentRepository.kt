package com.sultlab.murmur.domain.repository

import com.sultlab.murmur.data.model.Comment

interface CommentRepository {
    suspend fun getComments(postId: String): List<Comment>

    suspend fun addComment(
        postId: String,
        content: String,
        deviceHash: String,
    ): Comment

    suspend fun reportComment(
        commentId: String,
        deviceHash: String,
        reason: String,
    )
}
