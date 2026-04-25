package com.sultlab.murmur.data.local

expect class LikesStore {
    suspend fun addLike(postId: String)
    suspend fun removeLike(postId: String)
    suspend fun isLiked(postId: String): Boolean
    suspend fun getLikedPostIds(): Set<String>
}