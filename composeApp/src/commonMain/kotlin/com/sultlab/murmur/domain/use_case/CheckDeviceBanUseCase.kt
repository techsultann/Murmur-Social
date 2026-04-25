package com.sultlab.murmur.domain.use_case

import com.sultlab.murmur.data.local.DeviceHashStore
import com.sultlab.murmur.domain.repository.ModerationRepository
import kotlin.time.Instant

data class BanStatus(
    val isBanned: Boolean,
    val reason: String? = null,
    val expiresAt: Instant? = null,
)

class CheckDeviceBanUseCase(
    private val repo: ModerationRepository,
    private val deviceHashStore: DeviceHashStore,
) {
    suspend operator fun invoke(): BanStatus {
        val hash = deviceHashStore.getDeviceHash()
        return repo.checkBan(hash)
    }
}