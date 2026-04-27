# Token Registrar Service

The `TokenRegistrar` is a utility service responsible for managing and synchronizing Firebase Cloud Messaging (FCM) tokens with the Supabase backend. It ensures that each device is uniquely identified and can receive push notifications.

## Features
- **Token Registration**: Upserts the FCM token to the `push_tokens` table in Supabase.
- **Device Identification**: Uses `DeviceHashStore` to associate tokens with a unique device hash.
- **Conflict Handling**: Automatically handles conflicts by updating existing records based on the `device_hash`.
- **Automatic Fetching**: Provides a convenience method to fetch the current token from Firebase and register it in one go.

## Implementation Details

### `register(token: String)`
Sends the token along with the device hash and platform information to the backend.
- **Table**: `push_tokens`
- **Fields**: `device_hash`, `token`, `platform` (fixed to "android"), `updated_at`.

### `fetchAndRegister()`
A suspending function that:
1. Retrieves the current FCM token using `FirebaseMessaging.getInstance().token`.
2. Calls `register()` with the retrieved token.

## Usage in MainActivity
The registrar is typically invoked when the app starts and after notification permissions are granted:

```kotlin
private val tokenRegistrar: TokenRegistrar by inject()

// ... in permission callback or onCreate
lifecycleScope.launch {
    tokenRegistrar.fetchAndRegister()
}
```

## Screenshots
![App Screenshot Placeholder](screenshot.png)

---
*Generated for the Murmur project.*
