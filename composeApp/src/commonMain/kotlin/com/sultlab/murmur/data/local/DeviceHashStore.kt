package com.sultlab.murmur.data.local

// expect/actual pattern: each platform provides a DataStore<Preferences>
// backed by encrypted storage (EncryptedSharedPreferences on Android,
// Keychain-backed file on iOS via multiplatform-settings-secure or DataStore).
expect class DeviceHashStore {
    suspend fun getDeviceHash(): String
}

// ── Shared hashing logic ──────────────────────────────────────
// The actual hash is built in each platform's actual implementation
// from platform-specific signals, then stored here on first run.
// We expose only the final hex string to the rest of the app —
// no raw device identifiers ever leave the local module.