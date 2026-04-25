package com.sultlab.murmur.ui.navigation

import androidx.navigation3.runtime.NavKey
import com.sultlab.murmur.data.model.Post
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route: NavKey {

    @Serializable
    data object Onboarding: Route

    @Serializable
    data object Banned: Route

    @Serializable
    data object Feed: Route

    @Serializable
    data object Trending: Route

    @Serializable
    data object About: Route

    @Serializable
    data object ComposePost: Route

    @Serializable
    data class PostDetail(val post: Post): Route
}