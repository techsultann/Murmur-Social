package com.sultlab.murmur.domain.use_case

import com.sultlab.murmur.data.model.Comment
import com.sultlab.murmur.domain.repository.CommentRepository

class GetCommentsUseCase(private val repo: CommentRepository) {
    suspend operator fun invoke(postId: String): List<Comment> =
        repo.getComments(postId)
}