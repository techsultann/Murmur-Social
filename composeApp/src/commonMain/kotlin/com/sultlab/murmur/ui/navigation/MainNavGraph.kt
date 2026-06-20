package com.sultlab.murmur.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
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
import com.sultlab.murmur.ui.group.CreateGroupScreen
import com.sultlab.murmur.ui.group.GroupChatScreen
import com.sultlab.murmur.ui.group.GroupMembersScreen
import com.sultlab.murmur.ui.group.GroupsListScreen
import com.sultlab.murmur.ui.group.RecoverGroupScreen
import com.sultlab.murmur.ui.group.RecoveryPhraseScreen
import com.sultlab.murmur.ui.group.viewmodel.CreateGroupViewModel
import com.sultlab.murmur.ui.group.viewmodel.GroupChatViewModel
import com.sultlab.murmur.ui.group.viewmodel.GroupMembersViewModel
import com.sultlab.murmur.ui.group.viewmodel.GroupsListViewModel
import com.sultlab.murmur.ui.group.viewmodel.RecoverGroupViewModel
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
    val groupListViewModel: GroupsListViewModel = koinViewModel()

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
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

                    entry<Route.GroupList> {
                        GroupsListScreen(
                            viewModel = groupListViewModel,
                            onGroupClick = { group -> navigator.navigate(Route.GroupChat(group)) },
                            onCreateGroup = { navigator.navigate(Route.CreateGroup) },
                            onRecoverGroup = { navigator.navigate(Route.RecoverGroup) }
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

                    entry<Route.CreateGroup> {
                        val viewModel: CreateGroupViewModel = koinViewModel()
                        CreateGroupScreen(
                            viewModel = viewModel,
                            onBack = { navigator.goBack() },
                            onCreated = { group, phrase ->
                                navigator.navigate(Route.RecoveryPhrase(group, phrase))
                            }
                        )
                    }

                    entry<Route.RecoverGroup> {
                        val viewModel: RecoverGroupViewModel = koinViewModel()
                        RecoverGroupScreen(
                            viewModel = viewModel,
                            onBack = { navigator.goBack() },
                            onRecovered = { group ->
                                navigator.navigate(Route.GroupChat(group))
                            }
                        )
                    }

                    entry<Route.GroupChat> { route ->
                        val viewModel: GroupChatViewModel = koinViewModel(
                            parameters = { parametersOf(route.group) }
                        )
                        GroupChatScreen(
                            viewModel = viewModel,
                            onBack = { navigator.goBack() },
                            onManageMembers = {
                                navigator.navigate(Route.GroupMembers(route.group.id))
                            },
                            onDeleteMessage = { messageId ->
                                viewModel.onDeleteMessage(messageId)
                            }
                        )
                    }

                    entry<Route.GroupMembers> { route ->
                        val viewModel: GroupMembersViewModel = koinViewModel(
                            parameters = { parametersOf(route.groupId) }
                        )
                        GroupMembersScreen(
                            viewModel = viewModel,
                            onBack = { navigator.goBack() }
                        )
                    }

                    entry<Route.RecoveryPhrase> { route ->
                        val clipboardManager = LocalClipboardManager.current
                        RecoveryPhraseScreen(
                            group = route.group,
                            recoveryPhrase = route.phrase,
                            onCopyPhrase = {
                                clipboardManager.setText(AnnotatedString(route.phrase))
                            },
                            onShareJoinCode = {
                                // TODO: Implement sharing join code
                            },
                            onContinue = {
                                navigator.navigate(Route.GroupChat(route.group))
                            }
                        )
                    }
                }
            )
        )
    }
}
