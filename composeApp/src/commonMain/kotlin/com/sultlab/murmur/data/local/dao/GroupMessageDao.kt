package com.sultlab.murmur.data.local.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import com.sultlab.murmur.data.local.model.GroupMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupMessageDao {

    // Observe live messages for a group — UI reacts to this Flow
    @Query("""
        SELECT * FROM group_messages
        WHERE group_id = :groupId
          AND is_deleted = 0
        ORDER BY created_at ASC
    """)
    fun observeMessages(groupId: String): Flow<List<GroupMessageEntity>>

    // Load once (for initial hydration check)
    @Query("""
        SELECT * FROM group_messages
        WHERE group_id = :groupId
          AND is_deleted = 0
        ORDER BY created_at ASC
    """)
    suspend fun getMessages(groupId: String): List<GroupMessageEntity>

    @Upsert
    suspend fun upsert(message: GroupMessageEntity)

    @Upsert
    suspend fun upsertAll(messages: List<GroupMessageEntity>)

    // Soft-delete a single message (admin action)
    @Query("UPDATE group_messages SET is_deleted = 1 WHERE id = :messageId")
    suspend fun softDelete(messageId: String)

    // Delete messages older than 72 hours for a specific group
    @Query("""
        DELETE FROM group_messages
        WHERE group_id   = :groupId
          AND created_at < :cutoffMillis
    """)
    suspend fun deleteOlderThan(groupId: String, cutoffMillis: Long)

    // Delete all messages for a group (used when leaving or group is deleted)
    @Query("DELETE FROM group_messages WHERE group_id = :groupId")
    suspend fun deleteAllForGroup(groupId: String)

    // Get the timestamp of the newest cached message (for gap detection)
    @Query("""
        SELECT created_at FROM group_messages
        WHERE group_id = :groupId
        ORDER BY created_at DESC
        LIMIT 1
    """)
    suspend fun getLatestTimestamp(groupId: String): Long?
}