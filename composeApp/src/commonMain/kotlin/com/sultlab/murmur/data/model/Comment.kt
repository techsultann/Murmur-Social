package com.sultlab.murmur.data.model

import kotlin.time.Instant

data class Comment(
    val id: String,
    val postId: String,
    val content: String,
    val createdAt: Instant,
)


