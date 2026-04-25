package com.sultlab.murmur.data.repository

import com.sultlab.murmur.data.local.DeviceHashStore
import com.sultlab.murmur.data.mapper.toDomain
import com.sultlab.murmur.data.model.Comment
import com.sultlab.murmur.data.remote.CommentDto
import com.sultlab.murmur.domain.repository.CommentRepository
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class CommentRepositoryImpl(
    private val postgrest: Postgrest,
    private val deviceHashStore: DeviceHashStore
) : CommentRepository {


    override suspend fun getComments(postId: String): List<Comment> =
        postgrest["comments"]
            .select {
                filter {
                    eq("post_id", postId)
                    eq("status",  "visible")
                }
                order("created_at", Order.ASCENDING)
            }
            .decodeList<CommentDto>()
            .map { it.toDomain() }

    override suspend fun addComment(
        postId: String,
        content: String,
        deviceHash: String,
    ): Comment {
        val commentBody = buildJsonObject {
            put("post_id", postId)
            put("content", content)
            put("device_hash", deviceHash)
        }
        
        return postgrest["comments"]
            .insert(commentBody) { select() }
            .decodeSingle<CommentDto>()
            .toDomain()
    }

   override suspend fun reportComment(
        commentId: String,
        deviceHash: String,
        reason: String,
    ) {
        val reportBody = buildJsonObject {
            put("comment_id", commentId)
            put("device_hash", deviceHash)
            put("reason", reason)
        }
        postgrest["reports"].insert(reportBody)
    }
}
