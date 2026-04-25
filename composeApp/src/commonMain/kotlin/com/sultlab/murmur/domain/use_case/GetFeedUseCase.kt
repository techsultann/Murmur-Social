package com.sultlab.murmur.domain.use_case

import com.sultlab.murmur.data.model.Post
import com.sultlab.murmur.domain.repository.PostRepository
import kotlin.time.Instant

class GetFeedUseCase(private val repo: PostRepository) {
    suspend operator fun invoke(
        limit: Int = 20,
        before: Instant? = null,
    ): List<Post> = repo.getFeed(limit, before)
}