package com.sultlab.murmur

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.sultlab.murmur.ui.AppViewModel
import com.sultlab.murmur.ui.navigation.MainNavGraph
import com.sultlab.murmur.ui.navigation.Route
import com.sultlab.murmur.ui.theme.MurmurTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App(
    viewModel: AppViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    MurmurTheme {
        if (uiState.isReady) {
            val startRoute = when {
                uiState.banStatus.isBanned -> Route.Banned
                uiState.hasCompletedOnboarding -> Route.Feed
                else -> Route.Onboarding
            }
            MainNavGraph(
                startRoute = startRoute,
                onOnboardingComplete = {
                    viewModel.markOnboardingComplete()
                },
                banStatus = uiState.banStatus
            )
        }
    }
}
