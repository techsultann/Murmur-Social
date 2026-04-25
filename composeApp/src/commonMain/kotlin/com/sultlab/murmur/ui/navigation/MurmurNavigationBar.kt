package com.sultlab.murmur.ui.navigation

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource

@Composable
fun MurmurBottomNavigation(
    currentRoute: Route,
    onNavigate: (Route) -> Unit
) {

    NavigationBar(
        modifier = Modifier.height(74.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp
    ) {
        TOP_LEVEL_DESTINATIONS.forEach { (topLevelDestination, data) ->
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(data.selectedIcon),
                        contentDescription = data.title,
                        modifier = Modifier.size(24.dp)
                    )
                },
                alwaysShowLabel = true,
                label = { Text(text = data.title, style = MaterialTheme.typography.labelSmall) },
                selected = currentRoute == topLevelDestination,
                onClick = { onNavigate(topLevelDestination) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    }
}
