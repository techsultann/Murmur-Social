package com.sultlab.murmur.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sultlab.murmur.data.model.ReportReason
import murmur.composeapp.generated.resources.Res
import murmur.composeapp.generated.resources.chevron_right
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportSheet(
    onDismiss: () -> Unit,
    onReport: (ReportReason) -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(bottom = 32.dp)) {
            Text(
                text = "report this post",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
            )

            Spacer(Modifier.height(8.dp))

            reportOptions.forEach { option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onReport(option.reason) }
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text  = option.label,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text  = option.description,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Icon(
                        painter = painterResource(Res.drawable.chevron_right),
                        contentDescription = null,
                        tint  = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(18.dp),
                    )
                }
                HorizontalDivider(thickness = 0.5.dp)
            }

            Spacer(Modifier.height(8.dp))

            OutlinedButton(
                onClick  = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
            ) {
                Text("cancel")
            }
        }
    }
}

private data class ReportOption(
    val reason: ReportReason,
    val label: String,
    val description: String,
)

private val reportOptions = listOf(
    ReportOption(ReportReason.HARMFUL_OR_DANGEROUS,   "harmful or dangerous",   "content that could cause real harm"),
    ReportOption(ReportReason.HARASSMENT_OR_BULLYING, "harassment or bullying", "targeting an individual"),
    ReportOption(ReportReason.EXPLICIT_CONTENT,       "explicit content",       "sexual or graphic material"),
    ReportOption(ReportReason.SPAM_OR_BOT,            "spam or bot activity",   "repeated or automated posts"),
)