package com.sultlab.murmur.domain.repository

import com.sultlab.murmur.data.model.Post

interface PostRepository {
    suspend fun getFeed(
        limit: Int = 20,
        beforeCreatedAt: kotlin.time.Instant? = null,
    ): List<Post>

    suspend fun createPost(
        content: String,
        deviceHash: String,
        allowComments: Boolean,
    ): Result<Post>

    suspend fun likePost(postId: String, deviceHash: String)

    suspend fun unlikePost(postId: String, deviceHash: String)

    suspend fun toggleLike(post: Post, deviceHash: String)

    suspend fun reportPost(
        postId: String,
        deviceHash: String,
        reason: String,
    )
}
