package com.sultlab.murmur.ui.navigation

import murmur.composeapp.generated.resources.Res
import murmur.composeapp.generated.resources.about
import murmur.composeapp.generated.resources.home
import murmur.composeapp.generated.resources.paragraph
import murmur.composeapp.generated.resources.trending
import org.jetbrains.compose.resources.DrawableResource

data class BottomNavItem(
    val title: String,
    val selectedIcon: DrawableResource
)

val TOP_LEVEL_DESTINATIONS: Map<Route, BottomNavItem> = mapOf(
    Route.Feed to BottomNavItem(
        title = "Feed",
        selectedIcon = Res.drawable.home
    ),
    Route.Trending to BottomNavItem(
        title = "Trending",
        selectedIcon = Res.drawable.trending
    ),
    Route.About to BottomNavItem(
        title = "About",
        selectedIcon = Res.drawable.about
    ),
)
