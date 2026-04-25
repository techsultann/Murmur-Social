package com.sultlab.murmur.data.local

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import java.util.UUID

private val Context.dataStore by preferencesDataStore(name = "device_prefs")

private val KEY_DEVICE_HASH = stringPreferencesKey("device_hash")

// Salt to prevent rainbow-table lookup of ANDROID_ID values.
// Bake a unique salt into your app at build time — don't use this literal.
private const val HASH_SALT = "vs_salt_replace_with_build_constant"

actual class DeviceHashStore(private val context: Context) {

    @SuppressLint("HardwareIds")
    actual suspend fun getDeviceHash(): String {
        val prefs = context.dataStore.data.first()
        prefs[KEY_DEVICE_HASH]?.let { return it }

        // Build the fingerprint on first launch
        val androidId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID,
        ) ?: UUID.randomUUID().toString()

        val raw = "$HASH_SALT:$androidId"
        val hash = MessageDigest.getInstance("SHA-256")
            .digest(raw.toByteArray())
            .joinToString("") { "%02x".format(it) }

        context.dataStore.edit { it[KEY_DEVICE_HASH] = hash }
        return hash
    }
}
