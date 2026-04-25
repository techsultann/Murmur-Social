package com.sultlab.murmur.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.savedstate.compose.serialization.serializers.MutableStateSerializer
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

private val serializersConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(Route.Onboarding::class, Route.Onboarding.serializer())
            subclass(Route.Feed::class, Route.Feed.serializer())
            subclass(Route.Trending::class, Route.Trending.serializer())
            subclass(Route.About::class, Route.About.serializer())
            subclass(Route.ComposePost::class, Route.ComposePost.serializer())
            subclass(Route.PostDetail::class, Route.PostDetail.serializer())
        }
    }
}

class NavigationState(
    val startRoute: Route,
    topLevelDestinations: MutableState<Route>,
    val backStacks: Map<Route, NavBackStack<NavKey>>
) {

    var topLevelRoute: Route by topLevelDestinations

    val stacksInUse: List<Route>
        get() = if (topLevelRoute == startRoute) {
            listOf(startRoute)
        } else {
            listOf(startRoute, topLevelRoute)
        }
}

@Composable
fun rememberNavigationState(
    startRoute: Route,
    topLevelDestinations: Set<Route>
): NavigationState {

    @Suppress("UNCHECKED_CAST")
    val topLevelDestination = rememberSerializable(
        startRoute,
        topLevelDestinations,
        configuration = serializersConfig,
        serializer = MutableStateSerializer(PolymorphicSerializer(NavKey::class))
    ) {
        mutableStateOf(startRoute)
    } as MutableState<Route>

    val allDestinations = remember(startRoute, topLevelDestinations) {
        topLevelDestinations + startRoute
    }

    val backStacks: Map<Route, NavBackStack<NavKey>> =
        allDestinations.associateWith { route ->
            rememberNavBackStack(configuration = serializersConfig, route)
        }

    return remember(startRoute, topLevelDestinations) {
        NavigationState(
            startRoute = startRoute,
            topLevelDestinations = topLevelDestination,
            backStacks = backStacks
        )
    }
}

@Composable
fun NavigationState.toEntries(
    entryProvider: (NavKey) -> NavEntry<NavKey>
) : SnapshotStateList<NavEntry<NavKey>> {

    val decoratedEntries = backStacks.mapValues { (_, stack) ->
        val decorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator<NavKey>(),
            rememberViewModelStoreNavEntryDecorator()
        )

        rememberDecoratedNavEntries(
            backStack = stack,
            entryProvider = entryProvider,
            entryDecorators = decorators
        )
    }

    return stacksInUse
        .flatMap { decoratedEntries[it] ?: emptyList() }
        .toMutableStateList()
}
