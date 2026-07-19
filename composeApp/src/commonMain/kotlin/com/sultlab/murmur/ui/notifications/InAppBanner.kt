package com.sultlab.murmur.ui.notifications

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import murmur.composeapp.generated.resources.Res
import murmur.composeapp.generated.resources.comment_16
import murmur.composeapp.generated.resources.ic_chat_bubble
import murmur.composeapp.generated.resources.ic_close
import murmur.composeapp.generated.resources.ic_group
import murmur.composeapp.generated.resources.ic_notification
import murmur.composeapp.generated.resources.ic_person_add
import org.jetbrains.compose.resources.painterResource

@Composable
fun InAppBannerHost(
    manager: InAppNotificationManager,
    onBannerTap: (InAppNotification) -> Unit,
    modifier: Modifier = Modifier,
) {
    val notification by manager.current.collectAsStateWithLifecycle()

    Box(modifier = modifier) {
        AnimatedVisibility(
            visible = notification != null,
            enter   = slideInVertically { -it } + fadeIn(),
            exit    = slideOutVertically { -it } + fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter),
        ) {
            notification?.let { notif ->
                InAppBanner(
                    notification = notif,
                    onTap        = { onBannerTap(notif); manager.dismiss() },
                    onDismiss    = manager::dismiss,
                )
            }
        }
    }
}


@Composable
private fun InAppBanner(
    notification: InAppNotification,
    onTap: () -> Unit,
    onDismiss: () -> Unit,
) {
    val icon = when (notification.type) {
        InAppNotificationType.GROUP_MESSAGE -> Res.drawable.ic_chat_bubble
        InAppNotificationType.JOIN_REQUEST -> Res.drawable.ic_person_add
        InAppNotificationType.MEMBER_JOINED -> Res.drawable.ic_group
        InAppNotificationType.GENERAL -> Res.drawable.ic_notification
    }

    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = 8.dp,
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .fillMaxWidth()
            .clickable(onClick = onTap),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                shape  = MaterialTheme.shapes.medium,
                modifier = Modifier.size(36.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(icon),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = notification.title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = notification.body,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            IconButton(
                onClick  = onDismiss,
                modifier = Modifier.size(24.dp),
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_close),
                    contentDescription = "dismiss",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    }
}