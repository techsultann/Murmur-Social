package com.sultlab.murmur.di

import com.sultlab.murmur.BuildKonfig
import com.sultlab.murmur.data.repository.CommentRepositoryImpl
import com.sultlab.murmur.data.repository.GroupMessageRepositoryImpl
import com.sultlab.murmur.data.repository.GroupRepositoryImpl
import com.sultlab.murmur.data.repository.ModerationRepositoryImpl
import com.sultlab.murmur.data.repository.PostRealtimeRepository
import com.sultlab.murmur.data.repository.PostRepositoryImpl
import com.sultlab.murmur.domain.repository.CommentRepository
import com.sultlab.murmur.domain.repository.GroupMessageRepository
import com.sultlab.murmur.domain.repository.GroupRepository
import com.sultlab.murmur.domain.repository.ModerationRepository
import com.sultlab.murmur.domain.repository.PostRepository
import com.sultlab.murmur.domain.use_case.AddCommentUseCase
import com.sultlab.murmur.domain.use_case.ApproveJoinRequestUseCase
import com.sultlab.murmur.domain.use_case.CheckDeviceBanUseCase
import com.sultlab.murmur.domain.use_case.CheckIsGroupAdminUseCase
import com.sultlab.murmur.domain.use_case.ClearLocalMessagesUseCase
import com.sultlab.murmur.domain.use_case.CreateGroupUseCase
import com.sultlab.murmur.domain.use_case.CreatePostUseCase
import com.sultlab.murmur.domain.use_case.DeleteGroupMessageUseCase
import com.sultlab.murmur.domain.use_case.GetCommentsUseCase
import com.sultlab.murmur.domain.use_case.GetCurrentDeviceHashUseCase
import com.sultlab.murmur.domain.use_case.GetFeedUseCase
import com.sultlab.murmur.domain.use_case.GetGroupMembersUseCase
import com.sultlab.murmur.domain.use_case.GetJoinRequestsUseCase
import com.sultlab.murmur.domain.use_case.GetMyGroupsUseCase
import com.sultlab.murmur.domain.use_case.GroupMessageUseCases
import com.sultlab.murmur.domain.use_case.JoinGroupUseCase
import com.sultlab.murmur.domain.use_case.LikePostUseCase
import com.sultlab.murmur.domain.use_case.LoadAndCacheMessagesUseCase
import com.sultlab.murmur.domain.use_case.ObserveGroupMessagesUseCase
import com.sultlab.murmur.domain.use_case.RecoverGroupUseCase
import com.sultlab.murmur.domain.use_case.RejectJoinRequestUseCase
import com.sultlab.murmur.domain.use_case.RemoveGroupMemberUseCase
import com.sultlab.murmur.domain.use_case.ReportContentUseCase
import com.sultlab.murmur.domain.use_case.SearchGroupsUseCase
import com.sultlab.murmur.domain.use_case.SendGroupMessageUseCase
import com.sultlab.murmur.domain.use_case.SubscribeToGroupUseCase
import com.sultlab.murmur.domain.use_case.UnsubscribeFromGroupUseCase
import com.sultlab.murmur.ui.AppViewModel
import com.sultlab.murmur.ui.compose.ComposePostViewModel
import com.sultlab.murmur.ui.detail.PostDetailViewModel
import com.sultlab.murmur.ui.feed.FeedViewModel
import com.sultlab.murmur.ui.group.viewmodel.CreateGroupViewModel
import com.sultlab.murmur.ui.group.viewmodel.GroupChatViewModel
import com.sultlab.murmur.ui.group.viewmodel.GroupMembersViewModel
import com.sultlab.murmur.ui.group.viewmodel.GroupsListViewModel
import com.sultlab.murmur.ui.group.viewmodel.RecoverGroupViewModel
import com.sultlab.murmur.ui.trending.TrendingViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.realtime
import io.ktor.client.plugins.HttpTimeout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

@OptIn(SupabaseInternal::class)
val appModule = module {

    single<SupabaseClient> {
        createSupabaseClient(
            supabaseUrl = BuildKonfig.SUPABASE_URL,
            supabaseKey = BuildKonfig.SUPABASE_PUBLISHABLE_KEY
        ) {
            install(Postgrest)
            install(Functions)
            install(Realtime)

            httpConfig {
                install(HttpTimeout) {
                    requestTimeoutMillis = 30000 // 30 seconds
                    connectTimeoutMillis = 30000
                    socketTimeoutMillis = 30000
                }
            }
        }
    }

    single<Postgrest> { get<SupabaseClient>().postgrest }
    single<Functions> { get<SupabaseClient>().functions }
    single<Realtime> { get<SupabaseClient>().realtime }

    single<CoroutineScope>(named("AppScope")) {
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    // Repositories
    single {
        PostRealtimeRepository(
            supabase = get(),
            scope = get(named("AppScope")),
        )
    }
    single<PostRepository> { PostRepositoryImpl(get(), get(), get(), get()) }
    singleOf(::GroupRepositoryImpl) bind GroupRepository::class
    single<GroupMessageRepository> {
        GroupMessageRepositoryImpl(get(), get(), get(), get(named("AppScope")))
    }
    singleOf(::CommentRepositoryImpl) bind CommentRepository::class
    singleOf(::ModerationRepositoryImpl) bind ModerationRepository::class

    // Use Cases
    factoryOf(::GetFeedUseCase)
    factoryOf(::LikePostUseCase)
    factoryOf(::AddCommentUseCase)
    factoryOf(::CreatePostUseCase)
    factoryOf(::GetCommentsUseCase)
    factoryOf(::ReportContentUseCase)
    factoryOf(::CheckDeviceBanUseCase)

    // Group Use Cases
    factoryOf(::GetMyGroupsUseCase)
    factoryOf(::SearchGroupsUseCase)
    factoryOf(::CreateGroupUseCase)
    factoryOf(::JoinGroupUseCase)
    factoryOf(::RecoverGroupUseCase)
    factoryOf(::GetGroupMembersUseCase)
    factoryOf(::GetJoinRequestsUseCase)
    
    // Group Message Use Cases
    factoryOf(::ObserveGroupMessagesUseCase)
    factoryOf(::LoadAndCacheMessagesUseCase)
    factoryOf(::SendGroupMessageUseCase)
    factoryOf(::DeleteGroupMessageUseCase)
    factoryOf(::SubscribeToGroupUseCase)
    factoryOf(::UnsubscribeFromGroupUseCase)
    factoryOf(::ClearLocalMessagesUseCase)
    factory { 
        GroupMessageUseCases(
            observeMessages = get(),
            loadAndCache = get(),
            sendMessage = get(),
            deleteMessage = get(),
            subscribeToGroup = get(),
            unsubscribeFromGroup = get(),
            clearLocalMessages = get()
        )
    }

    factoryOf(::RemoveGroupMemberUseCase)
    factoryOf(::ApproveJoinRequestUseCase)
    factoryOf(::RejectJoinRequestUseCase)
    factoryOf(::CheckIsGroupAdminUseCase)
    factoryOf(::GetCurrentDeviceHashUseCase)

    // ViewModels
    viewModelOf(::AppViewModel)
    viewModelOf(::FeedViewModel)
    viewModelOf(::ComposePostViewModel)
    viewModel { params -> 
        PostDetailViewModel(
            initialPost = params.get(), 
            getComments = get(), 
            addComment = get(), 
            likePost = get(), 
            reportContent = get(),
            realtimeRepo = get(),
        ) 
    }
    viewModelOf(::TrendingViewModel)
    viewModelOf(::GroupsListViewModel)
    viewModel { params ->
        GroupMembersViewModel(
            groupId = params.get(),
            getMembers = get(),
            getJoinRequests = get(),
            removeMember = get(),
            approveJoinRequest = get(),
            rejectJoinRequest = get(),
            checkIsAdmin = get(),
        )
    }
    viewModel { params ->
        GroupChatViewModel(
            group = params.get(),
            groupMessage = get(),
            getMembers = get(),
            checkIsAdmin = get(),
        )
    }
    viewModel {
        RecoverGroupViewModel(
            recoverGroup = get()
        )
    }
    viewModel {
        CreateGroupViewModel(
            createGroup = get()
        )
    }
}

expect val platformModule: Module
