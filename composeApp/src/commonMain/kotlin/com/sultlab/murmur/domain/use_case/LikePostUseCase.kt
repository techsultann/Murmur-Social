package com.sultlab.murmur.domain.use_case

import co.touchlab.kermit.Logger
import com.sultlab.murmur.data.local.DeviceHashStore
import com.sultlab.murmur.data.model.Post
import com.sultlab.murmur.domain.repository.PostRepository

class LikePostUseCase(
    private val repo: PostRepository,
    private val deviceHashStore: DeviceHashStore
) {
    suspend operator fun invoke(post: Post) {
        val hash = deviceHashStore.getDeviceHash()
        Logger.d { "toggling like | post=${post.id} | likedByMe=${post.likedByMe} | hash=$hash" }
        repo.toggleLike(post, hash)
    }
}
