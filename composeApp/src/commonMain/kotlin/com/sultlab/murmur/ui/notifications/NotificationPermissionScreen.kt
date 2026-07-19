package com.sultlab.murmur.ui.notifications

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import murmur.composeapp.generated.resources.Res
import murmur.composeapp.generated.resources.ic_notification
import org.jetbrains.compose.resources.painterResource

@Composable
fun NotificationPermissionScreen(
    onAllow: () -> Unit,
    onSkip: () -> Unit,
) {
    Column(
        modifier            = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Surface(
            shape    = MaterialTheme.shapes.extraLarge,
            color    = MaterialTheme.colorScheme.surfaceVariant,
            border   = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.size(80.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(Res.drawable.ic_notification),
                    contentDescription = null,
                    modifier = Modifier.size(34.dp),
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Stay in the loop,\nanonymously",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            letterSpacing = (-0.3).sp,
        )

        Spacer(Modifier.height(10.dp))

        Text(
            text = "get notified when someone connects with your thoughts no identity attached, ever.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(24.dp))
        Column(
            modifier            = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            features.forEach { feature ->
                NotifFeatureRow(feature = feature)
            }
        }

        Spacer(Modifier.height(28.dp))

        Button(
            onClick  = onAllow,
            modifier = Modifier.fillMaxWidth(),
            shape    = MaterialTheme.shapes.large,
        ) {
            Text("allow notifications")
        }

        Spacer(Modifier.height(12.dp))

        TextButton(
            onClick = onSkip,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "not now",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview
@Composable
fun PreviewNotificationPermission(){
    NotificationPermissionScreen(
        onSkip = {},
        onAllow = {}
    )
}

@Composable
private fun NotifFeatureRow(feature: NotifFeature) {
    Surface(
        color = if (feature.muted) MaterialTheme.colorScheme.background
        else MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.large,
        border = if (feature.muted) null
        else BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .background(
                        color = if (feature.muted) MaterialTheme.colorScheme.outline
                        else MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.extraLarge,
                    )
            )
            Text(
                text  = feature.label,
                style = MaterialTheme.typography.bodySmall,
                color = if (feature.muted) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

private data class NotifFeature(val label: String, val muted: Boolean = false)

private val features = listOf(
    NotifFeature("Someone liked your post"),
    NotifFeature("Someone replied to your post"),
    NotifFeature("Someone joined your group"),
    NotifFeature("Group messages"),
    NotifFeature("No marketing, no account alerts", muted = true),
)