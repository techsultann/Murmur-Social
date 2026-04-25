package com.sultlab.murmur.domain.repository

import com.sultlab.murmur.domain.use_case.BanStatus

interface ModerationRepository {

    suspend fun checkBan(deviceHash: String) : BanStatus
}