package com.sultlab.murmur.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sultlab.murmur.ui.theme.Surface
import murmur.composeapp.generated.resources.Res
import murmur.composeapp.generated.resources.app_icon
import murmur.composeapp.generated.resources.comment_16
import murmur.composeapp.generated.resources.empty_reply
import murmur.composeapp.generated.resources.icon
import murmur.composeapp.generated.resources.trending
import org.jetbrains.compose.resources.painterResource

@Composable
fun EmptyState(
    icon: @Composable () -> Unit,
    title: String,
    body: String,
    action: EmptyStateAction? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 0.dp,
            modifier = Modifier.size(72.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                icon()
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = body,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        if (action != null) {
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = action.onClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(action.label)
            }
        }
    }
}

data class EmptyStateAction(val label: String, val onClick: () -> Unit)

@Composable
fun EmptyFeedState(onWritePost: () -> Unit) {
    EmptyState(
        icon   = {
            Icon(
                painter = painterResource(Res.drawable.app_icon),
                contentDescription = null,
                modifier = Modifier.size(50.dp),
                tint = Color.Unspecified
            )
        },
        title = "the space is quiet",
        body = "no thoughts have floated in yet. be the first voice in the space. no one will know it's you.",
        action = EmptyStateAction("write the first post", onWritePost),
    )
}

@Composable
fun EmptyCommentsState(modifier: Modifier = Modifier) {
    EmptyState(
        icon = {
            Icon(
                painter = painterResource(Res.drawable.empty_reply),
                contentDescription = null,
                tint  = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier  = Modifier.size(26.dp),
            )
        },
        title = "no replies yet",
        body = "be the first to respond. your reply is anonymous.",
        modifier = modifier,
    )
}

@Composable
fun EmptyTrendingState() {
    EmptyState(
        icon  = {
            Icon(
                painter = painterResource(Res.drawable.trending),
                contentDescription = null,
                tint  = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(28.dp),
            )
        },
        title = "nothing trending yet",
        body  = "trending posts appear here once enough voices join the void.",
    )
}