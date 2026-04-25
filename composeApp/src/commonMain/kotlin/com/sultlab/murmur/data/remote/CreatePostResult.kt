package com.sultlab.murmur.data.remote

import com.sultlab.murmur.data.model.Post

sealed interface CreatePostResult {
    data class Success(val post: Post) : CreatePostResult
    data class Failure(val message: String) : CreatePostResult
}