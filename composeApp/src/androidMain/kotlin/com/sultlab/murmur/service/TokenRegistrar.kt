package com.sultlab.murmur.service

import android.content.Context
import com.google.firebase.messaging.FirebaseMessaging
import com.sultlab.murmur.data.local.DeviceHashStore
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.tasks.await
import org.koin.core.context.GlobalContext
import kotlin.time.Clock

class TokenRegistrar(
    private val deviceHashStore: DeviceHashStore,
    private val supabase: SupabaseClient
) {

    suspend fun register(token: String) {
        val deviceHash = deviceHashStore.getDeviceHash()

        supabase.postgrest["push_tokens"].upsert(
            mapOf(
                "device_hash" to deviceHash,
                "token" to token,
                "platform" to "android",
                "updated_at" to Clock.System.now().toString()
            )
        ) {
            onConflict = "device_hash"
        }
    }

    suspend fun fetchAndRegister() {
        register(
            FirebaseMessaging.getInstance()
                .token
                .await()
        )
    }
}