package com.sultlab.murmur.data.remote

import kotlin.time.Instant

data class GroupMessage(
    val id: String,
    val groupId: String,
    val content: String,
    val deviceHash: String,
    val isFromAdmin: Boolean,
    val isOwnMessage: Boolean,
    val replyToId: String?   = null,
    val replyToContent: String? = null,
    val reactions: Map<String, List<String>> = emptyMap(),
    val createdAt: Instant,
) {
    // Convenience: get the reaction count for a specific emoji
    fun reactionCount(emoji: String): Int = reactions[emoji]?.size ?: 0

    // Convenience: check if current device has reacted with a specific emoji
    fun hasReacted(emoji: String, deviceHash: String): Boolean =
        reactions[emoji]?.contains(deviceHash) == true

    // All emojis that have at least one reaction, sorted for stable display
    val activeReactions: List<Pair<String, Int>>
        get() = reactions
            .filter { it.value.isNotEmpty() }
            .map { it.key to it.value.size }
            .sortedByDescending { it.second }
}