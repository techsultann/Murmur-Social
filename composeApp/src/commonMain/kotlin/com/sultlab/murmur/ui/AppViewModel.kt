package com.sultlab.murmur.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultlab.murmur.domain.use_case.BanStatus
import com.sultlab.murmur.domain.use_case.CheckDeviceBanUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppViewModel(
    private val checkBan: CheckDeviceBanUseCase,
    private val prefs: AppPreferences,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    init {
        launch()
    }

    private fun launch() {
        viewModelScope.launch {
            val onboardingDone = prefs.hasCompletedOnboarding()
            val notifPromptShown = prefs.hasShownNotifPrompt()

            // Check device ban on every cold start
            val ban = runCatching { checkBan() }.getOrDefault(BanStatus(false))

            _uiState.update {
                it.copy(
                    isReady = true,
                    banStatus = ban,
                    hasCompletedOnboarding = onboardingDone,
                    hasShownNotifPrompt = notifPromptShown,
                )
            }
        }
    }

    fun markOnboardingComplete() {
        viewModelScope.launch {
            prefs.setOnboardingComplete()
            _uiState.update { it.copy(hasCompletedOnboarding = true) }
        }
    }

    fun markNotifPromptShown() {
        viewModelScope.launch {
            prefs.setNotifPromptShown()
            _uiState.update { it.copy(hasShownNotifPrompt = true) }
        }
    }
}

data class AppUiState(
    val isReady: Boolean                 = false,
    val banStatus: BanStatus = BanStatus(isBanned = false),
    val hasCompletedOnboarding: Boolean  = false,
    val hasShownNotifPrompt: Boolean     = false,
)

expect class AppPreferences {
    suspend fun hasCompletedOnboarding(): Boolean
    suspend fun setOnboardingComplete()
    suspend fun hasShownNotifPrompt(): Boolean
    suspend fun setNotifPromptShown()
}