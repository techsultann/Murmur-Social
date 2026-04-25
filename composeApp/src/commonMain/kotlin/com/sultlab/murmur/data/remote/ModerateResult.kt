package com.sultlab.murmur.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ModerateResult(
    val allowed: Boolean,
    val reason: String? = null,
    @SerialName("toxicity_score") val toxicityScore: Double? = null,
)