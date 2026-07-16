package com.sultlab.murmur.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateGroupResponse(
    val group: GroupSummaryDto? = null,
    @SerialName("recovery_phrase") val recoveryPhrase: String? = null,
    val error: String? = null,
)