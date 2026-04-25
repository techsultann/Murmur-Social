package com.sultlab.murmur.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class BanCheckResponse(
    val banned: Boolean,
    val reason: String? = null,
    @SerialName("expires_at") val expiresAt: Instant? = null,
)