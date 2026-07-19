package com.sultlab.murmur.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.sultlab.murmur.ui.detail.PostDetailByIdViewModel
import com.sultlab.murmur.ui.detail.PostDetailByIdUiState
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
import com.sultlab.murmur.ui.group.viewmodel.GroupChatByIdViewModel
import com.sultlab.murmur.ui.group.viewmodel.GroupChatByIdUiState
import com.sultlab.murmur.ui.group.viewmodel.GroupMembersViewModel
import com.sultlab.murmur.ui.group.viewmodel.GroupsListViewModel
import com.sultlab.murmur.ui.group.viewmodel.RecoverGroupViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.wrapContentSize
import com.sultlab.murmur.ui.group.GroupAdminScreen
import com.sultlab.murmur.ui.group.viewmodel.GroupAdminViewModel
import com.sultlab.murmur.ui.notifications.InAppBannerHost
import com.sultlab.murmur.ui.notifications.InAppNotificationManager
import com.sultlab.murmur.ui.notifications.InAppNotificationType
import com.sultlab.murmur.ui.notifications.NotificationPermissionScreen
import com.sultlab.murmur.ui.onboard.OnboardingScreen
import com.sultlab.murmur.ui.trending.TrendingScreen
import com.sultlab.murmur.ui.trending.TrendingViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun MainNavGraph(
    startRoute: Route,
    onOnboardingComplete: () -> Unit,
    onNotifPromptShown: () -> Unit,
    banStatus: BanStatus,
    pendingDeepLink: String? = null,
    onDeepLinkHandled: () -> Unit = {},
    onRequestNotificationPermission: () -> Unit = {}
){

    val navigationState = rememberNavigationState(
        startRoute = startRoute,
        topLevelDestinations = TOP_LEVEL_DESTINATIONS.keys
    )
    val navigator = remember(navigationState) { Navigator(navigationState) }

    val trendingViewModel: TrendingViewModel = koinViewModel()
    val feedViewModel: FeedViewModel = koinViewModel()
    val groupListViewModel: GroupsListViewModel = koinViewModel()

    LaunchedEffect(pendingDeepLink) {
        if (pendingDeepLink != null) {
            val uri = pendingDeepLink
            // murmur://post/{postId}
            if (uri.startsWith("murmur://post/")) {
                val postId = uri.substringAfter("murmur://post/")
                navigator.navigate(Route.PostDetailById(postId))
            }
            // murmur://group/{groupId}
            else if (uri.startsWith("murmur://group/") && !uri.endsWith("/members")) {
                val groupId = uri.substringAfter("murmur://group/")
                navigator.navigate(Route.GroupChatById(groupId))
            }
            // murmur://group/{groupId}/members
//            else if (uri.startsWith("murmur://group/") && uri.endsWith("/members")) {
//                val groupId = uri.substringAfter("murmur://group/").substringBefore("/members")
//                navigator.navigate(Route.GroupMembers(groupId))
//            }
            onDeepLinkHandled()
        }
    }

    Box(modifier = Modifier.fillMaxSize()){
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
                                    navigator.navigate(Route.NotificationPermission)
                                    onOnboardingComplete()
                                }
                            )
                        }

                        entry<Route.NotificationPermission>{
                            NotificationPermissionScreen(
                                onSkip = {
                                    navigator.navigate(Route.Feed)
                                    onNotifPromptShown()
                                },
                                onAllow = {
                                    onRequestNotificationPermission()
                                    navigator.navigate(Route.Feed)
                                    onNotifPromptShown()
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

                        entry<Route.PostDetailById> { route ->
                            val viewModel: PostDetailByIdViewModel = koinViewModel(
                                parameters = { parametersOf(route.postId) }
                            )
                            val uiState by viewModel.uiState.collectAsState()
                            when (val state = uiState) {
                                is PostDetailByIdUiState.Loading -> {
                                    CircularProgressIndicator(
                                        modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center)
                                    )
                                }
                                is PostDetailByIdUiState.Success -> {
                                    val detailViewModel: PostDetailViewModel = koinViewModel(
                                        parameters = { parametersOf(state.post) }
                                    )
                                    PostDetailScreen(
                                        onBack = { navigator.goBack() },
                                        viewModel = detailViewModel
                                    )
                                }
                                is PostDetailByIdUiState.Error -> {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text(text = state.message)
                                    }
                                }
                            }
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
                                onAdminSettings = {
                                    navigator.navigate(Route.GroupAdminSettings(route.group))
                                },
                                onDeleteMessage = { messageId ->
                                    viewModel.deleteMessage(messageId)
                                }
                            )
                        }

                        entry<Route.GroupChatById> { route ->
                            val viewModel: GroupChatByIdViewModel = koinViewModel(
                                parameters = { parametersOf(route.groupId) }
                            )
                            val uiState by viewModel.uiState.collectAsState()
                            when (val state = uiState) {
                                is GroupChatByIdUiState.Loading -> {
                                    CircularProgressIndicator(
                                        modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center)
                                    )
                                }
                                is GroupChatByIdUiState.Success -> {
                                    val chatViewModel: GroupChatViewModel = koinViewModel(
                                        parameters = { parametersOf(state.group) }
                                    )
                                    GroupChatScreen(
                                        viewModel = chatViewModel,
                                        onBack = { navigator.goBack() },
                                        onAdminSettings = {
                                            navigator.navigate(Route.GroupAdminSettings(state.group))
                                        },
                                        onDeleteMessage = { messageId ->
                                            chatViewModel.deleteMessage(messageId)
                                        }
                                    )
                                }
                                is GroupChatByIdUiState.Error -> {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text(text = state.message)
                                    }
                                }
                            }
                        }

                        entry<Route.GroupAdminSettings> { route ->
                            val viewModel: GroupAdminViewModel = koinViewModel(
                                parameters = { parametersOf(route.group) }
                            )
                            GroupAdminScreen(
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
        val notifManager = koinInject<InAppNotificationManager>()
        InAppBannerHost(
            manager = notifManager,
            onBannerTap = { notif ->
                notif.groupId?.let { groupId ->
                    when (notif.type) {
                        InAppNotificationType.JOIN_REQUEST,
//                        InAppNotificationType.MEMBER_JOINED ->
//                            navigator.navigate(Route.GroupMembers(groupId))
                        InAppNotificationType.GROUP_MESSAGE -> {
                            groupListViewModel.uiState.value.myGroups.find { it.id == groupId }?.let { group ->
                                navigator.navigate(Route.GroupChat(group))
                            }
                        }
                        else -> Unit
                    }
                }
            },
            modifier = Modifier.fillMaxSize(),
        )
    }
}
