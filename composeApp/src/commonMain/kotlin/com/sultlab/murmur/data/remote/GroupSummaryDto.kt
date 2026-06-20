package com.sultlab.murmur.data.remote

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class GroupSummaryDto(
    val id: String,
    @SerialName("join_code") val joinCode: String,
    val name: String,
    val visibility: String,
)