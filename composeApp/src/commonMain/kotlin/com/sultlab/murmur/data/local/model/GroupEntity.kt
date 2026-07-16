package com.sultlab.murmur.data.local.model

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.sultlab.murmur.data.model.GroupMemberRole
import com.sultlab.murmur.data.model.GroupMemberStatus
import com.sultlab.murmur.data.model.GroupVisibility

@Entity(tableName = "groups")
data class GroupEntity(
    @PrimaryKey
    val id: String,
    val joinCode: String,
    val name: String,
    val description: String?,
    val visibility: GroupVisibility,
    val memberCount: Int,
    val messageCount: Int,
    val createdAt: Long,
    val lastActiveAt: Long,
    val myRole: GroupMemberRole?,
    val myStatus: GroupMemberStatus?,
)
