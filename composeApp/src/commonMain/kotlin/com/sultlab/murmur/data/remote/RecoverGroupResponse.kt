package com.sultlab.murmur.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class RecoverGroupResponse(
    val status: String? = null,
    val group: GroupSummaryDto? = null,
    val error: String? = null,
)