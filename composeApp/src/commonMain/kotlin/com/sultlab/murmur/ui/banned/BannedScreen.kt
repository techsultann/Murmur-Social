package com.sultlab.murmur.ui.banned

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sultlab.murmur.domain.use_case.BanStatus
import com.sultlab.murmur.ui.theme.Accent
import murmur.composeapp.generated.resources.Res
import murmur.composeapp.generated.resources.prohibited
import org.jetbrains.compose.resources.painterResource
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

@Composable
fun BannedScreen(
    banStatus: BanStatus,
    onReadPolicy: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {

        // Red circular icon matching the screenshot
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = Color(0xFF1A0A0A),
            border = BorderStroke(1.dp, Color(0xFF3A1515)),
            modifier = Modifier.size(80.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(Res.drawable.prohibited),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp),
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        Text(
            text = "your voice is paused",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            ),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "this device has been temporarily restricted from posting due to a content policy violation.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(32.dp))

        BanDetailsCard(banStatus = banStatus)

        Spacer(Modifier.height(32.dp))

        Text(
            text = "you can still read and like posts. replying and creating new posts is disabled until the ban lifts.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(Modifier.height(24.dp))

        TextButton(onClick = onReadPolicy) {
            Text(
                text = "read our content policy",
                color = Color(0xFF8B8BFF), // Link color
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun BanDetailsCard(banStatus: BanStatus) {
    Surface(
        color = Color(0xFF110808),
        border = BorderStroke(0.5.dp, Color(0xFF2A1010)),
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            BanDetailRow(label = "reason", value = banStatus.reason ?: "policy violation")
            
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                thickness = 0.5.dp,
                color = Color(0xFF2A1010)
            )
            
            val isTemporary = banStatus.expiresAt != null
            if (isTemporary) {
                BanDetailRow(
                    label = "lifted in",
                    value = banStatus.expiresAt!!.toCountdownString(),
                    valueColor = Color(0xFFEF9F27) // Orange color
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 0.5.dp,
                    color = Color(0xFF2A1010)
                )
            }
            
            BanDetailRow(
                label = "type",
                value = if (isTemporary) "temporary" else "permanent",
                valueColor = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
private fun BanDetailRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onBackground,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = valueColor,
        )
    }
}


private fun Instant.toCountdownString(): String {
    val remaining = this - Clock.System.now()
    return when {
        remaining.inWholeDays > 0 -> {
            val d = remaining.inWholeDays
            val h = (remaining - d.days).inWholeHours
            "${d} days, ${h} hours"
        }
        remaining.inWholeHours > 0 -> "${remaining.inWholeHours} hours remaining"
        remaining.inWholeMinutes > 0 -> "${remaining.inWholeMinutes} minutes remaining"
        else -> "lifting soon"
    }
}
