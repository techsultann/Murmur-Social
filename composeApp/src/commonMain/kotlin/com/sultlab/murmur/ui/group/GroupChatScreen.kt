package com.sultlab.murmur.ui.group

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sultlab.murmur.data.model.GroupMessage
import com.sultlab.murmur.ui.group.viewmodel.GroupChatViewModel
import com.sultlab.murmur.ui.theme.Dark
import murmur.composeapp.generated.resources.Res
import murmur.composeapp.generated.resources.chat_backgroud_2
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

    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Box(modifier = Modifier.fillMaxSize()){
        Image(
            painter = painterResource(Res.drawable.chat_backgroud_2),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Scaffold(
            contentWindowInsets = WindowInsets(0.dp),
            containerColor = Color.Transparent,
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
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        actionIconContentColor = MaterialTheme.colorScheme.onBackground
                    ),
                )
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .imePadding()
                    .fillMaxSize()
            ){
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
                            modifier = Modifier.fillMaxSize().padding(padding),
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
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .padding(padding),
                            contentPadding = PaddingValues(vertical = 10.dp),
                            reverseLayout = false
                        ) {
                            items(
                                items = uiState.messages,
                                key = { it.id }
                            ) { message ->
                                GroupMessageRow(
                                    message = message,
                                    isFromMe = message.deviceHash == uiState.currentDeviceHash,
                                    canDelete = uiState.isCurrentDeviceAdmin,
                                    onDelete = { onDeleteMessage(message.id) },
                                )
                            }
                        }
                    }
                }

                MessageInputBar(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    value = uiState.messageInput,
                    onValueChange = viewModel::onMessageInputChange,
                    onSend = viewModel::send,
                    isSending = uiState.isSending,
                )
            }
        }
    }

}

@Composable
private fun GroupMessageRow(
    message: GroupMessage,
    isFromMe: Boolean,
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
        horizontalAlignment = if (isFromMe) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (isFromMe) Arrangement.End else Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (!isFromMe && message.isFromAdmin) {
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
                text  = if (isFromMe) "me · ${message.createdAt.toRelativeLabel()}" else "anonymous · ${message.createdAt.toRelativeLabel()}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = if (isFromMe) TextAlign.End else TextAlign.Start
            )
            if (isFromMe && message.isFromAdmin) {
                Spacer(Modifier.width(6.dp))
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
            }
        }

        Spacer(Modifier.height(4.dp))

        Surface(
            shape = MaterialTheme.shapes.large.copy(
                bottomEnd = if (isFromMe) CornerSize(0.dp) else CornerSize(16.dp),
                bottomStart = if (isFromMe) CornerSize(16.dp) else CornerSize(0.dp)
            ),
            color    = if (isFromMe) MaterialTheme.colorScheme.primary else if (message.isFromAdmin) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.widthIn(max = 280.dp),
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp),
                color = if (isFromMe) MaterialTheme.colorScheme.onPrimary else if (message.isFromAdmin) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
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
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    isSending: Boolean,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .wrapContentHeight()
                .weight(1f)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.extraLarge
                )
                .padding(horizontal = 16.dp, vertical = 8.dp),
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                lineHeight = 16.sp
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                capitalization = KeyboardCapitalization.Sentences
            ),
            maxLines = 5,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurfaceVariant),
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) {
                        Text(
                            text = "Message the group...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    innerTextField()
                }
            }
        )
        IconButton(
            onClick = onSend,
            enabled = value.isNotBlank() && !isSending,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onBackground
            )
        ) {
            if (isSending) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            } else {
                Icon(
                    painter = painterResource(Res.drawable.send),
                    contentDescription = "send",
                    modifier = Modifier.size(30.dp),
                    tint = Dark
                )
            }
        }
    }
}

private fun Instant.toRelativeLabel(): String {
    val diff = Clock.System.now() - this
    return when {
        diff < kotlin.time.Duration.parse("1m") -> "just now"
        diff < kotlin.time.Duration.parse("1h") -> "${diff.inWholeMinutes}m ago"
        diff < kotlin.time.Duration.parse("1d") -> "${diff.inWholeHours}h ago"
        else -> "${diff.inWholeDays}d ago"
    }
}