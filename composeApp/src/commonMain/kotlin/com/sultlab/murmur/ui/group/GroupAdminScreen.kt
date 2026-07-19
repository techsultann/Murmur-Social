package com.sultlab.murmur.ui.group

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sultlab.murmur.data.model.Group
import com.sultlab.murmur.data.model.GroupJoinRequest
import com.sultlab.murmur.ui.group.viewmodel.GroupAdminViewModel
import murmur.composeapp.generated.resources.Res
import murmur.composeapp.generated.resources.chevron_backward
import murmur.composeapp.generated.resources.ic_check
import murmur.composeapp.generated.resources.ic_check_circle
import murmur.composeapp.generated.resources.ic_close
import murmur.composeapp.generated.resources.ic_content_copy
import murmur.composeapp.generated.resources.ic_key
import murmur.composeapp.generated.resources.ic_visibility
import murmur.composeapp.generated.resources.ic_visibility_off
import org.jetbrains.compose.resources.painterResource
import kotlin.time.Clock
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupAdminScreen(
    viewModel: GroupAdminViewModel,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val clipboard  = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("group settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(Res.drawable.chevron_backward), contentDescription = "back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {

            GroupInfoCard(
                group         = uiState.group,
                memberCount   = uiState.memberCount,
                copiedJoinCode = uiState.copiedJoinCode,
                onCopyJoinCode = {
                    clipboard.setText(AnnotatedString(uiState.group.joinCode))
                    viewModel.onJoinCodeCopied()
                },
            )


            RecoveryPhraseCard(
                phrase          = uiState.recoveryPhrase,
                isLoading       = uiState.isLoadingPhrase,
                isVisible       = uiState.phraseVisible,
                copied          = uiState.copiedPhrase,
                onToggleVisible = viewModel::togglePhraseVisibility,
                onCopy          = {
                    uiState.recoveryPhrase?.let { phrase ->
                        clipboard.setText(AnnotatedString(phrase))
                        viewModel.onPhraseCopied()
                    }
                },
            )

            if (uiState.group.visibility.name == "PRIVATE") {
                JoinRequestsCard(
                    requests   = uiState.joinRequests,
                    isLoading  = uiState.isLoadingRequests,
                    onApprove  = viewModel::approveRequest,
                    onReject   = viewModel::rejectRequest,
                )
            }

            uiState.error?.let { msg ->
                Text(
                    text  = msg,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun GroupInfoCard(
    group: Group,
    memberCount: Int,
    copiedJoinCode: Boolean,
    onCopyJoinCode: () -> Unit,
) {
    Surface(
        color  = MaterialTheme.colorScheme.surfaceVariant,
        shape  = MaterialTheme.shapes.large,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text  = "group info",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(10.dp))
            InfoRow(label = "name",    value = group.name)
            InfoRow(label = "members", value = "$memberCount anonymous members")
            InfoRow(label = "type",    value = group.visibility.name.lowercase())
            Spacer(Modifier.height(12.dp))

            // Join code with copy button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text  = "join code",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text  = group.joinCode,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            letterSpacing = 1.sp,
                        ),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                OutlinedButton(
                    onClick = onCopyJoinCode,
                    shape   = MaterialTheme.shapes.large,
                ) {
                    Icon(
                        painter = if (copiedJoinCode) painterResource(Res.drawable.ic_check_circle) else painterResource(Res.drawable.ic_content_copy),
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = if (copiedJoinCode) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(if (copiedJoinCode) "copied" else "copy code")
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text  = value,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun RecoveryPhraseCard(
    phrase: String?,
    isLoading: Boolean,
    isVisible: Boolean,
    copied: Boolean,
    onToggleVisible: () -> Unit,
    onCopy: () -> Unit,
) {
    Surface(
        color  = MaterialTheme.colorScheme.surfaceVariant,
        shape  = MaterialTheme.shapes.large,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.onSurfaceVariant),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.size(28.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_key),
                            contentDescription = null,
                            tint  = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }
                Text(
                    text  = "recovery phrase",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text  = "use this to regain admin access if you switch devices. copy it anytime.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(14.dp))

            AnimatedContent(
                targetState = Triple(isLoading, isVisible, phrase),
                label       = "phrase_state",
            ) { (loading, visible, p) ->
                when {
                    loading -> {
                        Box(
                            modifier         = Modifier.fillMaxWidth().height(120.dp),
                            contentAlignment = Alignment.Center,
                        ) { CircularProgressIndicator(strokeWidth = 2.dp) }
                    }

                    visible && p != null -> {
                        PhraseWordGrid(phrase = p)
                    }

                    else -> {
                        // Hidden state — blurred placeholder
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "tap to reveal your recovery phrase",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick  = onToggleVisible,
                    shape    = MaterialTheme.shapes.large,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        painter = if (isVisible) painterResource(Res.drawable.ic_visibility_off)
                        else painterResource(Res.drawable.ic_visibility),
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(if (isVisible) "hide" else "reveal phrase")
                }

                if (isVisible && phrase != null) {
                    Button(
                        onClick = onCopy,
                        shape   = MaterialTheme.shapes.large,
                        colors  = ButtonDefaults.buttonColors(
                            containerColor = if (copied) MaterialTheme.colorScheme.surface
                            else MaterialTheme.colorScheme.primary,
                        ),
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(
                            painter = if (copied) painterResource(Res.drawable.ic_check) else painterResource(Res.drawable.ic_content_copy),
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (copied) MaterialTheme.colorScheme.primary else Color.White,
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text  = if (copied) "copied!" else "copy",
                            color = if (copied) MaterialTheme.colorScheme.primary else Color.White,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PhraseWordGrid(phrase: String) {
    val words = phrase.split(" ")
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement   = Arrangement.spacedBy(8.dp),
        modifier            = Modifier.heightIn(max = 280.dp),
    ) {
        itemsIndexed(words) { index, word ->
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.medium,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text  = "${index + 1}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.width(16.dp),
                    )
                    Text(
                        text  = word,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun JoinRequestsCard(
    requests: List<GroupJoinRequest>,
    isLoading: Boolean,
    onApprove: (String) -> Unit,
    onReject: (String) -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.large,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text  = "join requests${if (requests.isNotEmpty()) " · ${requests.size}" else ""}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(12.dp))

            when {
                isLoading -> {
                    Box(
                        modifier         = Modifier.fillMaxWidth().padding(16.dp),
                        contentAlignment = Alignment.Center,
                    ) { CircularProgressIndicator(strokeWidth = 2.dp) }
                }

                requests.isEmpty() -> {
                    Text(
                        text  = "no pending requests",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                else -> {
                    requests.forEach { request ->
                        JoinRequestRow(
                            request   = request,
                            onApprove = { onApprove(request.deviceHash) },
                            onReject  = { onReject(request.deviceHash) },
                        )
                        if (request != requests.last()) {
                            HorizontalDivider(
                                thickness = 0.5.dp,
                                modifier  = Modifier.padding(vertical = 4.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun JoinRequestRow(
    request: GroupJoinRequest,
    onApprove: () -> Unit,
    onReject: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.size(32.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    "an",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text("anonymous", style = MaterialTheme.typography.bodySmall)
            Text(
                text  = "requested ${request.requestedAt.toRelativeLabel()}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        IconButton(onClick = onApprove, modifier = Modifier.size(32.dp)) {
            Icon(
                painter = painterResource(Res.drawable.ic_check),
                contentDescription = "approve",
                tint               = Color(0xFF1D9E75),
                modifier           = Modifier.size(18.dp),
            )
        }
        IconButton(onClick = onReject, modifier = Modifier.size(32.dp)) {
            Icon(
                painter = painterResource(Res.drawable.ic_close),
                contentDescription = "reject",
                tint               = MaterialTheme.colorScheme.error,
                modifier           = Modifier.size(18.dp),
            )
        }
    }
}

private fun Instant.toRelativeLabel(): String {
    val diff = Clock.System.now() - this
    return when {
        diff.inWholeMinutes < 1  -> "just now"
        diff.inWholeHours < 1    -> "${diff.inWholeMinutes}m ago"
        diff.inWholeDays < 1     -> "${diff.inWholeHours}h ago"
        else                     -> "${diff.inWholeDays}d ago"
    }
}