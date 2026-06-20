package com.sultlab.murmur.ui.group

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sultlab.murmur.data.model.GroupMessage
import com.sultlab.murmur.ui.group.viewmodel.GroupChatViewModel
import murmur.composeapp.generated.resources.Res
import murmur.composeapp.generated.resources.chevron_backward
import murmur.composeapp.generated.resources.ic_group
import murmur.composeapp.generated.resources.send
import org.jetbrains.compose.resources.painterResource
import kotlin.time.Clock
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupChatScreen(
    viewModel: GroupChatViewModel,
    onBack: () -> Unit,
    onManageMembers: () -> Unit,
    onDeleteMessage: (messageId: String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceDim
                        )
                    ) {
                        Icon(painterResource(Res.drawable.chevron_backward), contentDescription = "back")
                    }
                },
                title = {
                    Column(
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = uiState.group.name,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text  = "${uiState.group.memberCount} members",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onManageMembers) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_group),
                            contentDescription = "members"
                        )
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
        bottomBar = {
            MessageInputBar(
                value         = uiState.messageInput,
                onValueChange = viewModel::onMessageInputChange,
                onSend        = viewModel::send,
                isSending     = uiState.isSending,
            )
        },
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.messages.isEmpty() -> {
                Box(
                    modifier         = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "no messages yet, say something to start the conversation.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(32.dp),
                    )
                }
            }

            else -> {
                LazyColumn(
                    state = listState,
                    modifier  = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(vertical = 10.dp),
                ) {
                    items(
                        items = uiState.messages,
                        key = { it.id }
                    ) { message ->
                        GroupMessageRow(
                            message = message,
                            canDelete = uiState.isCurrentDeviceAdmin,
                            onDelete = { onDeleteMessage(message.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GroupMessageRow(
    message: GroupMessage,
    canDelete: Boolean,
    onDelete: () -> Unit,
) {
    var showActions by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 6.dp)
            .let {
                if (canDelete) it.combinedClickable(
                    onClick      = {},
                    onLongClick  = { showActions = true },
                ) else it
            },
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (message.isFromAdmin) {
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Text(
                        text     = "admin",
                        style    = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color    = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                    )
                }
                Spacer(Modifier.width(6.dp))
            }
            Text(
                text  = "anonymous · ${message.createdAt.toRelativeLabel()}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(Modifier.height(4.dp))

        Surface(
            shape = MaterialTheme.shapes.large,
            color    = if (message.isFromAdmin) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.widthIn(max = 280.dp),
        ) {
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp),
            )
        }
    }

    if (showActions && canDelete) {
        AlertDialog(
            onDismissRequest = { showActions = false },
            title            = { Text("delete this message?") },
            text             = { Text("this will remove the message for everyone in the group.") },
            confirmButton    = {
                TextButton(onClick = { onDelete(); showActions = false }) {
                    Text("delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton    = {
                TextButton(onClick = { showActions = false }) { Text("cancel") }
            },
        )
    }
}

@Composable
private fun MessageInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    isSending: Boolean,
) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder   = { Text("message the group…") },
            singleLine = false,
            maxLines = 4,
            shape = MaterialTheme.shapes.extraLarge,
            modifier  = Modifier.weight(1f),
        )
        IconButton(
            onClick = onSend,
            enabled = value.isNotBlank() && !isSending,
        ) {
            if (isSending) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            } else {
                Icon(
                    painter = painterResource(Res.drawable.send), contentDescription = "send")
            }
        }
    }
}

// ── Time helper ───────────────────────────────────────────────

private fun Instant.toRelativeLabel(): String {
    val diff = Clock.System.now() - this
    return when {
        diff < kotlin.time.Duration.parse("1m") -> "just now"
        diff < kotlin.time.Duration.parse("1h") -> "${diff.inWholeMinutes}m ago"
        diff < kotlin.time.Duration.parse("1d") -> "${diff.inWholeHours}h ago"
        else -> "${diff.inWholeDays}d ago"
    }
}