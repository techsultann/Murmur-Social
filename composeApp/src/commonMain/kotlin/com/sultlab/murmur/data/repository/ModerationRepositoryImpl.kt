package com.sultlab.murmur.data.repository

import com.sultlab.murmur.data.remote.BanCheckResponse
import com.sultlab.murmur.domain.repository.ModerationRepository
import com.sultlab.murmur.domain.use_case.BanStatus
import io.github.jan.supabase.functions.Functions
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class ModerationRepositoryImpl(
    private val functions: Functions
) : ModerationRepository {

    override suspend fun checkBan(deviceHash: String): BanStatus {
        val response = functions.invoke(
            function = "check-device-ban",
            body = buildJsonObject { put("device_hash", deviceHash) },
        )
        val dto = Json.decodeFromString<BanCheckResponse>(response.bodyAsText())
        return BanStatus(
            isBanned  = dto.banned,
            reason    = dto.reason,
            expiresAt = dto.expiresAt,
        )
    }
}