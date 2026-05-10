package com.sultlab.murmur.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.sultlab.murmur.domain.use_case.BanStatus
import com.sultlab.murmur.ui.about.AboutScreen
import com.sultlab.murmur.ui.banned.BannedScreen
import com.sultlab.murmur.ui.compose.ComposePostScreen
import com.sultlab.murmur.ui.compose.ComposePostViewModel
import com.sultlab.murmur.ui.detail.PostDetailScreen
import com.sultlab.murmur.ui.detail.PostDetailViewModel
import com.sultlab.murmur.ui.feed.FeedScreen
import com.sultlab.murmur.ui.feed.FeedViewModel
import com.sultlab.murmur.ui.onboard.OnboardingScreen
import com.sultlab.murmur.ui.trending.TrendingScreen
import com.sultlab.murmur.ui.trending.TrendingViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun MainNavGraph(
    startRoute: Route,
    onOnboardingComplete: () -> Unit,
    banStatus: BanStatus
){

    val navigationState = rememberNavigationState(
        startRoute = startRoute,
        topLevelDestinations = TOP_LEVEL_DESTINATIONS.keys
    )
    val navigator = remember { Navigator(navigationState) }

    val trendingViewModel: TrendingViewModel = koinViewModel()
    val feedViewModel: FeedViewModel = koinViewModel()

    Scaffold(
        bottomBar = {
            val currentRoute = navigationState.backStacks[navigationState.topLevelRoute]?.lastOrNull()
            if (currentRoute in TOP_LEVEL_DESTINATIONS.keys) {
                MurmurBottomNavigation(
                    currentRoute = navigationState.topLevelRoute,
                    onNavigate = { route -> navigator.navigate(route) }
                )
            }
        }
    ) { paddingValues ->
        NavDisplay(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            onBack = navigator::goBack,
            entries = navigationState.toEntries(
                entryProvider {

                    entry<Route.Onboarding> {
                        OnboardingScreen(
                            onGetStartedClick = { 
                                onOnboardingComplete()
                                navigator.navigate(Route.Feed) 
                            }
                        )
                    }
                    
                    entry<Route.Banned> {
                        BannedScreen(
                            banStatus = banStatus,
                            onReadPolicy = {}
                        )
                    }
                    entry<Route.Feed> {
                        FeedScreen(
                            viewModel = feedViewModel,
                            onPostClick = { post -> navigator.navigate(Route.PostDetail(post)) },
                            onComposePostClick = { navigator.navigate(Route.ComposePost) }
                        )
                    }

                    entry<Route.Trending> {
                        TrendingScreen(
                            viewModel = trendingViewModel,
                            onPostClick = { post -> navigator.navigate(Route.PostDetail(post)) }
                        )
                    }

                    entry<Route.About> {
                        AboutScreen(
                            appVersion = "1.0.0",
                            onContentPolicy = {},
                            onPrivacyInfo = {}
                        )
                    }

                    entry<Route.ComposePost> {
                        val viewModel: ComposePostViewModel = koinViewModel()
                        ComposePostScreen(
                            onDismiss = { navigator.goBack() },
                            viewModel = viewModel,
                            onPostSuccess = { feedViewModel.loadFeed() }
                        )
                    }
                    entry<Route.PostDetail> { route ->
                        val viewModel: PostDetailViewModel = koinViewModel(
                            parameters = { parametersOf(route.post) }
                        )
                        PostDetailScreen(
                            onBack = { navigator.goBack() },
                            viewModel = viewModel
                        )
                    }
                }
            )
        )
    }
}