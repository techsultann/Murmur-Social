package com.sultlab.murmur.domain.use_case

import co.touchlab.kermit.Logger
import com.sultlab.murmur.data.local.DeviceHashStore
import com.sultlab.murmur.data.remote.CreatePostResult
import com.sultlab.murmur.domain.repository.PostRepository

class CreatePostUseCase(
    private val repo: PostRepository,
    private val deviceHashStore: DeviceHashStore
) {

    suspend fun execute(
        content: String,
        allowComments: Boolean,
    ): CreatePostResult {
        if (content.isBlank()) {
            return CreatePostResult.Failure("Post cannot be empty.")
        }
        if (content.length > 500) {
            return CreatePostResult.Failure("Post exceeds 500 characters.")
        }
        return repo.createPost(content.trim(), deviceHash = deviceHashStore.getDeviceHash() , allowComments).fold(
            onSuccess = {
                Logger.d("Post created successfully: $it")
                CreatePostResult.Success(it)
                        },
            onFailure = {
                Logger.d("Post creation failed: ${it.message}")
                CreatePostResult.Failure(it.message ?: "Something went wrong.")
                        },
        )
    }
}