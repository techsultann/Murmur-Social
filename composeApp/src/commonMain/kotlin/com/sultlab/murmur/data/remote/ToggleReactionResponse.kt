package com.sultlab.murmur.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class ToggleReactionResponse(
    val reactions: Map<String, List<String>>,
    val emoji: String,
    val action: String,
)