package com.sultlab.murmur.data.local.model

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey val id: String,
    val content: String,
    val allowComments: Boolean,
    val status: String,
    val likeCount: Int,
    val commentCount: Int,
    val createdAt: Long
)
