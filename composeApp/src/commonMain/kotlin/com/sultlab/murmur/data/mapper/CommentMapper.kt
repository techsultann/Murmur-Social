package com.sultlab.murmur.data.mapper

import com.sultlab.murmur.data.model.Comment
import com.sultlab.murmur.data.remote.CommentDto

fun CommentDto.toDomain() = Comment(
    id = id,
    postId = postId,
    content = content,
    createdAt = createdAt,
)