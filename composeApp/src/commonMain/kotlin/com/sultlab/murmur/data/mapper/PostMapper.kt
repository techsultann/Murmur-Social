package com.sultlab.murmur.data.mapper

import com.sultlab.murmur.data.model.Post
import com.sultlab.murmur.data.model.PostStatus
import com.sultlab.murmur.data.remote.PostDto
import com.sultlab.murmur.data.remote.PostRealtimeDto
import kotlin.time.Instant

fun PostDto.toDomain() = Post(
    id = id,
    content = content,
    allowComments = allowComments,
    status = PostStatus.valueOf(status.uppercase()),
    likeCount = likeCount,
    commentCount = commentCount,
    createdAt = createdAt,
)

fun PostRealtimeDto.toDomain(likedByMe: Boolean = false) = Post(
    id            = id,
    content       = content,
    allowComments = allowComments,
    status        = PostStatus.valueOf(status.uppercase()),
    likeCount     = likeCount,
    commentCount  = commentCount,
    createdAt     = Instant.parse(createdAt),
    likedByMe     = likedByMe,
)