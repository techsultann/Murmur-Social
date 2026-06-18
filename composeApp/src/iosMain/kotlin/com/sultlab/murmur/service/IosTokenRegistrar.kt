package com.sultlab.murmur.service

import com.sultlab.murmur.data.local.DeviceHashStore
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Clock

class IosTokenRegistrar : KoinComponent {
    private val deviceHashStore: DeviceHashStore by inject()
    private val supabase: SupabaseClient by inject()
    private val scope = CoroutineScope(Dispatchers.Default)

    fun register(token: String) {
        scope.launch {
            try {
                val deviceHash = deviceHashStore.getDeviceHash()
                supabase.postgrest["push_tokens"].upsert(
                    mapOf(
                        "device_hash" to deviceHash,
                        "token" to token,
                        "platform" to "ios",
                        "updated_at" to Clock.System.now().toString()
                    )
                ) {
                    onConflict = "device_hash"
                }
            } catch (e: Exception) {
                println("Failed to register iOS token: ${e.message}")
            }
        }
    }
}
