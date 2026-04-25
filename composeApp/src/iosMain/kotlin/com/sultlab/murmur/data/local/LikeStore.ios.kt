package com.sultlab.murmur.data.local

import platform.Foundation.NSUserDefaults

actual class LikesStore {

    private val defaults = NSUserDefaults.standardUserDefaults
    private val key = "murmur_liked_ids"

    actual suspend fun addLike(postId: String) {
        val current = getLikedPostIds().toMutableSet()
        current.add(postId)
        defaults.setObject(current.toList(), key)
    }

    actual suspend fun removeLike(postId: String) {
        val current = getLikedPostIds().toMutableSet()
        current.remove(postId)
        defaults.setObject(current.toList(), key)
    }

    actual suspend fun isLiked(postId: String): Boolean =
        getLikedPostIds().contains(postId)

    actual suspend fun getLikedPostIds(): Set<String> {
        val list = defaults.arrayForKey(key) as? List<String> ?: emptyList()
        return list.toSet()
    }

}