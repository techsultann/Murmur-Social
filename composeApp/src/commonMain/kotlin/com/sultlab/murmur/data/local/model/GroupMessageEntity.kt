package com.sultlab.murmur.data.local.model

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey


@Entity(
    tableName = "group_messages",
    indices   = [
        Index(value = ["group_id", "created_at"]),
        Index(value = ["created_at"]),
    ]
)
data class GroupMessageEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "group_id")
    val groupId: String,

    @ColumnInfo(name = "device_hash")
    val deviceHash: String,

    val content: String,

    @ColumnInfo(name = "is_admin")
    val isAdmin: Boolean,

    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,

    // Stored as epoch milliseconds for easy comparison
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
)