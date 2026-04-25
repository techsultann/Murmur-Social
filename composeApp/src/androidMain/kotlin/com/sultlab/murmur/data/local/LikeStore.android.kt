package com.sultlab.murmur.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.likesDataStore by preferencesDataStore("murmur_likes")

actual class LikesStore(private val context: Context) {

    actual suspend fun addLike(postId: String) {
        context.likesDataStore.edit { prefs ->
            val current = prefs[stringSetPreferencesKey("liked_ids")]?.toMutableSet() ?: mutableSetOf()
            current.add(postId)
            prefs[stringSetPreferencesKey("liked_ids")] = current
        }
    }

    actual suspend fun removeLike(postId: String) {
        context.likesDataStore.edit { prefs ->
            val current = prefs[stringSetPreferencesKey("liked_ids")]?.toMutableSet() ?: mutableSetOf()
            current.remove(postId)
            prefs[stringSetPreferencesKey("liked_ids")] = current
        }
    }

    actual suspend fun isLiked(postId: String): Boolean {
        val prefs = context.likesDataStore.data.first()
        return prefs[stringSetPreferencesKey("liked_ids")]?.contains(postId) == true
    }

    actual suspend fun getLikedPostIds(): Set<String> {
        val prefs = context.likesDataStore.data.first()
        return prefs[stringSetPreferencesKey("liked_ids")] ?: emptySet()
    }
}