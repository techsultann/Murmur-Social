package com.sultlab.murmur.ui

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "app_settings")

actual class AppPreferences(private val context: Context) {
    private val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
    private val NOTIF_PROMPT_SHOWN = booleanPreferencesKey("notif_prompt_shown")

    actual suspend fun hasCompletedOnboarding(): Boolean {
        return context.dataStore.data.map { it[ONBOARDING_COMPLETE] ?: false }.first()
    }

    actual suspend fun setOnboardingComplete() {
        context.dataStore.edit { it[ONBOARDING_COMPLETE] = true }
    }

    actual suspend fun hasShownNotifPrompt(): Boolean {
        return context.dataStore.data.map { it[NOTIF_PROMPT_SHOWN] ?: false }.first()
    }

    actual suspend fun setNotifPromptShown() {
        context.dataStore.edit { it[NOTIF_PROMPT_SHOWN] = true }
    }
}
