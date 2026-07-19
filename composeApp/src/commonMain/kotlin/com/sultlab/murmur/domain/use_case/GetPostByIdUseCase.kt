package com.sultlab.murmur.domain.use_case

import com.sultlab.murmur.data.model.Post
import com.sultlab.murmur.domain.repository.PostRepository

class GetPostByIdUseCase(private val repository: PostRepository) {
    suspend operator fun invoke(postId: String): Result<Post> {
        return repository.getPostById(postId)
    }
}
