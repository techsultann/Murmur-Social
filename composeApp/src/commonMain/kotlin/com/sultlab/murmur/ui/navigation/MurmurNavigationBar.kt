package com.sultlab.murmur.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource

@Composable
fun MurmurBottomNavigation(
    currentRoute: Route,
    onNavigate: (Route) -> Unit
) {

    NavigationBar(
        modifier = Modifier.wrapContentHeight(),
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        tonalElevation = 0.dp,
        windowInsets = NavigationBarDefaults.windowInsets
    ) {
        TOP_LEVEL_DESTINATIONS.forEach { (topLevelDestination, data) ->
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(data.selectedIcon),
                        contentDescription = data.title,
                        modifier = Modifier.size(22.dp)
                    )
                },
                alwaysShowLabel = true,
                label = { Text(text = data.title, style = MaterialTheme.typography.labelSmall) },
                selected = currentRoute == topLevelDestination,
                onClick = { onNavigate(topLevelDestination) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.secondary,
                    selectedTextColor = MaterialTheme.colorScheme.secondary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}
