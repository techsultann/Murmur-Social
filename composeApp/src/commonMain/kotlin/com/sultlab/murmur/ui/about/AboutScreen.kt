package com.sultlab.murmur.ui.about

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.sultlab.murmur.ui.components.AnonymousPost
import com.sultlab.murmur.ui.components.MurMurTopBar
import com.sultlab.murmur.ui.theme.Surface
import murmur.composeapp.generated.resources.Res
import murmur.composeapp.generated.resources.chevron_right
import murmur.composeapp.generated.resources.info
import murmur.composeapp.generated.resources.no_accounts
import murmur.composeapp.generated.resources.paragraph
import murmur.composeapp.generated.resources.prohibited
import murmur.composeapp.generated.resources.resource_public
import murmur.composeapp.generated.resources.verified
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun AboutScreen(
    appVersion: String,
    onContentPolicy: () -> Unit,
    onPrivacyInfo: () -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AppIconMark(size = 56.dp)
                Spacer(Modifier.height(10.dp))
                Text(
                    buildAnnotatedString {
                        append("MUR")
                        withStyle(
                            SpanStyle(color = MaterialTheme.colorScheme.primary)
                        ) { append("MUR") }
                    },
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text  = "version $appVersion",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            HorizontalDivider(thickness = 0.5.dp)
        }

        item {
            AboutSection(
                heading = "our promise",
                items   = listOf(
                    AboutItem(
                        icon = Res.drawable.no_accounts,
                        label = "no account required",
                        subtitle = "write and read without signing up",
                    ),
                    AboutItem(
                        icon = Res.drawable.verified,
                        label  = "zero personal data",
                        subtitle = "we don't know who you are",
                    ),
                ),
            )
        }

        item {
            AboutSection(
                heading = "legal",
                items   = listOf(
                    AboutItem(
                        icon = Res.drawable.paragraph,
                        label    = "content policy",
                        subtitle = "what's allowed in the void",
                        tappable = true,
                        onClick  = onContentPolicy,
                    ),
                    AboutItem(
                        icon = Res.drawable.info,
                        label = "privacy information",
                        subtitle = "what we collect (spoiler: nothing)",
                        tappable = true,
                        onClick  = onPrivacyInfo,
                    ),
                ),
            )
        }

        // ── Footer ────────────────────────────────────────────
        item {
            Text(
                text      = "MURMUR $appVersion · made with intention",
                style     = MaterialTheme.typography.labelSmall,
                color     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                textAlign = TextAlign.Center,
                modifier  = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
            )
        }
    }
}

@Composable
private fun AboutSection(heading: String, items: List<AboutItem>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text     = heading,
            style    = MaterialTheme.typography.labelSmall,
            color    = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(
                start  = 18.dp,
                top    = 16.dp,
                bottom = 8.dp,
            ),
        )
        items.forEach { item ->
            AboutRow(item = item)
            HorizontalDivider(
                thickness = 0.5.dp,
                modifier  = Modifier.padding(start = 62.dp),
            )
        }
    }
}

@Composable
private fun AboutRow(item: AboutItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (item.tappable && item.onClick != null)
                    Modifier.clickable(onClick = item.onClick)
                else Modifier
            )
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(32.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(item.icon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier  = Modifier.size(14.dp),
                )
            }
        }

        // Labels
        Column(modifier = Modifier.weight(1f)) {
            Text(text = item.label,    style = MaterialTheme.typography.bodySmall)
            Text(
                text  = item.subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Chevron for tappable rows
        if (item.tappable) {
            Icon(
                painter = painterResource(Res.drawable.chevron_right),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun AppIconMark(size: androidx.compose.ui.unit.Dp) {
    Surface(
        shape    = MaterialTheme.shapes.large,
        color    = MaterialTheme.colorScheme.background,
        border   = androidx.compose.foundation.BorderStroke(
            0.5.dp, MaterialTheme.colorScheme.outline
        ),
        modifier = Modifier.size(size),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(Res.drawable.resource_public),
                contentDescription = "voidspace",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(size * 0.5f),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private data class AboutItem(
    val icon: DrawableResource,
    val label: String,
    val subtitle: String,
    val tappable: Boolean = false,
    val onClick: (() -> Unit)? = null,
)