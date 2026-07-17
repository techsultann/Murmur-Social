package com.sultlab.murmur.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class ReplyPreviewDto(
    val id: String,
    val content: String,
)