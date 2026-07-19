package com.sultlab.murmur

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.sultlab.murmur.ui.AppViewModel
import com.sultlab.murmur.ui.group.GroupNotificationObserver
import com.sultlab.murmur.ui.navigation.MainNavGraph
import com.sultlab.murmur.ui.navigation.Route
import com.sultlab.murmur.ui.theme.MurmurTheme
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

import androidx.compose.runtime.key

@Composable
fun App(
    viewModel: AppViewModel = koinViewModel(),
    onRequestNotificationPermission: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val observer = koinInject<GroupNotificationObserver>()

    LaunchedEffect(Unit) {
        observer.start()
    }

    MurmurTheme {
        if (uiState.isReady) {
            val startRoute = when {
                uiState.banStatus.isBanned -> Route.Banned
                !uiState.hasCompletedOnboarding -> Route.Onboarding
                !uiState.hasShownNotifPrompt -> Route.NotificationPermission
                else -> Route.Feed
            }
            key(startRoute) {
                MainNavGraph(
                    startRoute = startRoute,
                    onOnboardingComplete = {
                        viewModel.markOnboardingComplete()
                    },
                    onNotifPromptShown = {
                        viewModel.markNotifPromptShown()
                    },
                    banStatus = uiState.banStatus,
                    pendingDeepLink = uiState.pendingDeepLink,
                    onDeepLinkHandled = {
                        viewModel.consumeDeepLink()
                    },
                    onRequestNotificationPermission = onRequestNotificationPermission
                )
            }
        }
    }
}
