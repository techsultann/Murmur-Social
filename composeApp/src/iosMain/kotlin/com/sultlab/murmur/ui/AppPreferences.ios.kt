package com.sultlab.murmur.ui

import platform.Foundation.NSUserDefaults

actual class AppPreferences {
    private val userDefaults = NSUserDefaults.standardUserDefaults
    private val ONBOARDING_COMPLETE = "onboarding_complete"
    private val NOTIF_PROMPT_SHOWN = "notif_prompt_shown"

    actual suspend fun hasCompletedOnboarding(): Boolean {
        return userDefaults.boolForKey(ONBOARDING_COMPLETE)
    }

    actual suspend fun setOnboardingComplete() {
        userDefaults.setBool(true, forKey = ONBOARDING_COMPLETE)
    }

    actual suspend fun hasShownNotifPrompt(): Boolean {
        return userDefaults.boolForKey(NOTIF_PROMPT_SHOWN)
    }

    actual suspend fun setNotifPromptShown() {
        userDefaults.setBool(true, forKey = NOTIF_PROMPT_SHOWN)
    }
}
