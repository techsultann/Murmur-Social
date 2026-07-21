package com.sultlab.murmur.ui.group

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sultlab.murmur.data.remote.GroupMessage
import com.sultlab.murmur.ui.group.viewmodel.GroupChatViewModel
import com.sultlab.murmur.ui.theme.Dark
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.blurEffect
import dev.chrisbanes.haze.blur.materials.HazeMaterials
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.launch
import murmur.composeapp.generated.resources.Res
import murmur.composeapp.generated.resources.chat_backgroud_2
import murmur.composeapp.generated.resources.chevron_backward
import murmur.composeapp.generated.resources.ic_add_reaction
import murmur.composeapp.generated.resources.ic_close
import murmur.composeapp.generated.resources.ic_content_copy
import murmur.composeapp.generated.resources.ic_delete
import murmur.composeapp.generated.resources.ic_group
import murmur.composeapp.generated.resources.ic_more
import murmur.composeapp.generated.resources.ic_notification
import murmur.composeapp.generated.resources.ic_reply
import murmur.composeapp.generated.resources.ic_settings
import murmur.composeapp.generated.resources.send
import org.jetbrains.compose.resources.painterResource
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupChatScreen(
    viewModel: GroupChatViewModel,
    onBack: () -> Unit,
    onAdminSettings: () -> Unit,
    onDeleteMessage: (messageId: String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val hazeState = rememberHazeState()
    val scope = rememberCoroutineScope()
    var screenHeight by remember { mutableStateOf(0) }
    var showMore by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    LaunchedEffect(uiState.group.id) {
        ActiveGroupScreen.currentGroupId.value = uiState.group.id
    }

    DisposableEffect(Unit) {
        onDispose {
            ActiveGroupScreen.currentGroupId.value = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .hazeSource(hazeState)
            .onGloballyPositioned { coordinates ->
                screenHeight = coordinates.size.height
            }
    ) {
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
                        Box {
                            IconButton(
                                onClick = { showMore = true },
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceDim
                                )
                            ) {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_more),
                                    contentDescription = "more",
                                    modifier = Modifier.size(25.dp)
                                )
                            }
                            DropdownMenu(
                                expanded = showMore,
                                onDismissRequest = { showMore = false },
                                containerColor = MaterialTheme.colorScheme.background,
                                modifier = Modifier.width(200.dp)
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Text(if (uiState.isMuted) "Unmute" else "Mute")
                                    },
                                    onClick = {
                                        viewModel.toggleMute()
                                        showMore = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(Res.drawable.ic_notification),
                                            contentDescription = "mute",
                                            tint = if (uiState.isMuted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                )
                                if (uiState.isCurrentDeviceAdmin) {
                                    DropdownMenuItem(
                                        text = {
                                            Text("Settings")
                                        },
                                        onClick = {
                                            onAdminSettings()
                                            showMore = false
                                        },
                                        leadingIcon = {
                                            Icon(
                                                painter = painterResource(Res.drawable.ic_settings),
                                                contentDescription = "settings",
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    )
                                }
                            }
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
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(padding),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    uiState.messages.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(padding),
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
                                    currentDeviceHash = uiState.currentDeviceHash,
                                    canDelete = uiState.isCurrentDeviceAdmin,
                                    onDelete = { onDeleteMessage(message.id) },
                                    onReply = { viewModel.onReply(message) },
                                    onToggleReaction = { emoji ->
                                        viewModel.toggleReaction(message.id, emoji)
                                                       },
                                    hazeState = hazeState,
                                    screenHeight = screenHeight,
                                )
                            }
                        }
                    }
                }
                Column {
                    AnimatedVisibility(
                        visible = uiState.replyingTo != null,
                        enter   = slideInVertically { it },
                        exit    = slideOutVertically { it },
                    ) {
                        uiState.replyingTo?.let { reply ->
                            ReplyPreviewBar(
                                content   = reply.content,
                                onCancel  = viewModel::clearReply,
                            )
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
}

@Composable
private fun GroupMessageRow(
    message: GroupMessage,
    isFromMe: Boolean,
    currentDeviceHash: String,
    canDelete: Boolean,
    onDelete: () -> Unit,
    onReply: () -> Unit,
    onToggleReaction: (String) -> Unit,
    hazeState: HazeState,
    screenHeight: Int,
) {
    var showActions by remember { mutableStateOf(false) }
    val isOwn = isFromMe
    var showPopup by remember { mutableStateOf(false) }
    var messageOffset by remember { mutableStateOf(Offset.Zero) }
    var messageSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current
    val blurStyle = HazeMaterials.thin()
    val clipboardManager = LocalClipboardManager.current

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
        Box(
            modifier = Modifier
                .onGloballyPositioned { coordinates ->
                    messageOffset = coordinates.positionInWindow()
                    messageSize = coordinates.size
                }
                .combinedClickable(
                    onClick     = {},
                    onLongClick = { showPopup = true },
                )
        ){
            Box(
                modifier = Modifier
            ){
                Surface(
                    shape = MaterialTheme.shapes.large.copy(
                        bottomEnd = if (isFromMe) CornerSize(0.dp) else CornerSize(16.dp),
                        bottomStart = if (isFromMe) CornerSize(16.dp) else CornerSize(0.dp)
                    ),
                    color    = if (isFromMe) MaterialTheme.colorScheme.primary else if (message.isFromAdmin) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.widthIn(max = 280.dp),
                ) {
                    Column(modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp)) {
                        if (message.replyToId != null && message.replyToContent != null) {
                            ReplyQuote(
                                content = message.replyToContent,
                                isOwn = isOwn,
                            )
                            Spacer(Modifier.height(4.dp))
                        }
                        Text(
                            text = message.content,
                            color = if (isFromMe) MaterialTheme.colorScheme.onPrimary else if (message.isFromAdmin) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (message.activeReactions.isNotEmpty()) {
                    ReactionsRow(
                        modifier = Modifier
                            .align(
                                if (isFromMe)
                                    Alignment.BottomEnd
                                else
                                    Alignment.BottomStart
                            )
                            .offset(
                                x = if (isFromMe) (-10).dp else 10.dp,
                                y = (30).dp
                            ),
                        reactions = message.activeReactions,
                        currentDeviceHash = currentDeviceHash,
                        messageReactions  = message.reactions,
                        onToggle  = onToggleReaction,
                    )
                }
            }

            if (showPopup) {
                Popup(
                    onDismissRequest = { showPopup = false },
                    properties = PopupProperties(focusable = true)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .hazeEffect(state = hazeState) {
                                blurEffect {
                                    this.style = blurStyle
                                }
                            }
                            .background(Color.Black.copy(alpha = 0.2f))
                            .clickable { showPopup = false }
                    ) {
                        // Reactions Row above the message
                        Row(
                            modifier = Modifier
                                .offset {
                                    val rowWidth = with(density) { 260.dp.roundToPx() }
                                    var x = if (isFromMe) {
                                        (messageOffset.x.toInt() + messageSize.width - rowWidth)
                                    } else {
                                        messageOffset.x.toInt()
                                    }

                                    // Screen clamping for X
                                    x = x.coerceIn(with(density) { 16.dp.roundToPx() }, 2000)

                                    // Show above the bubble
                                    val y = messageOffset.y.toInt() - with(density) { 56.dp.roundToPx() }

                                    IntOffset(x, y.coerceAtLeast(with(density) { 16.dp.roundToPx() }))
                                }
                                .clip(MaterialTheme.shapes.extraLarge)
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                .clickable(enabled = false) { }
                        ) {
                            commonEmojis.forEach { emoji ->
                                Text(
                                    text = emoji,
                                    fontSize = 22.sp,
                                    modifier = Modifier
                                        .clickable {
                                            onToggleReaction(emoji)
                                            showPopup = false
                                        }
                                        .padding(horizontal = 4.dp)
                                )
                            }
                        }

                        // Highlight the original message bubble
                        Box(
                            modifier = Modifier
                                .offset { IntOffset(messageOffset.x.toInt(), messageOffset.y.toInt()) }
                                .width(with(density) { messageSize.width.toDp() })
                        ) {
                            Surface(
                                shape = MaterialTheme.shapes.large.copy(
                                    bottomEnd = if (isFromMe) CornerSize(0.dp) else CornerSize(16.dp),
                                    bottomStart = if (isFromMe) CornerSize(16.dp) else CornerSize(0.dp)
                                ),
                                color = if (isFromMe) MaterialTheme.colorScheme.primary else if (message.isFromAdmin) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp)) {
                                    if (message.replyToId != null && message.replyToContent != null) {
                                        ReplyQuote(
                                            content = message.replyToContent,
                                            isOwn = isOwn,
                                        )
                                        Spacer(Modifier.height(4.dp))
                                    }
                                    Text(
                                        text = message.content,
                                        color = if (isFromMe) MaterialTheme.colorScheme.onPrimary else if (message.isFromAdmin) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            if (message.activeReactions.isNotEmpty()) {
                                ReactionsRow(
                                    modifier = Modifier
                                        .align(
                                            if (isFromMe)
                                                Alignment.BottomEnd
                                            else
                                                Alignment.BottomStart
                                        )
                                        .offset(
                                            x = if (isFromMe) (-10).dp else 10.dp,
                                            y = (30).dp
                                        ),
                                    reactions = message.activeReactions,
                                    currentDeviceHash = currentDeviceHash,
                                    messageReactions  = message.reactions,
                                    onToggle  = onToggleReaction,
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .offset {
                                    val menuWidth = with(density) { 220.dp.roundToPx() }
                                    var x = if (isFromMe) {
                                        (messageOffset.x.toInt() + messageSize.width - menuWidth)
                                    } else {
                                        messageOffset.x.toInt()
                                    }
                                    x = x.coerceIn(with(density) { 16.dp.roundToPx() }, 2000)

                                    val reactionsOffset = if (message.activeReactions.isNotEmpty()) with(density) { 40.dp.roundToPx() } else 0
                                    
                                    // Logic to show above or below
                                    val threshold = screenHeight / 2
                                    val y = if (messageOffset.y.toInt() > threshold) {
                                        messageOffset.y.toInt() - with(density) { 180.dp.roundToPx() } // Show above
                                    } else {
                                        messageOffset.y.toInt() + messageSize.height + with(density) { 8.dp.roundToPx() } + reactionsOffset
                                    }

                                    IntOffset(x, y)
                                }
                                .width(220.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(vertical = 8.dp)
                                .clickable(enabled = false) { }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Reply") },
                                onClick = { onReply(); showPopup = false },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(Res.drawable.ic_reply),
                                        contentDescription = "reply",
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("Copy") },
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(message.content))
                                    showPopup = false
                                          },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(Res.drawable.ic_content_copy),
                                        contentDescription = "copy",
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                            )
                            if (canDelete) {
                                DropdownMenuItem(
                                    text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                                    onClick = { onDelete(); showPopup = false },
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(Res.drawable.ic_delete),
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
        Spacer(
            Modifier.height(
                if (message.activeReactions.isNotEmpty()) 30.dp
                else 0.dp
            )
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

@Composable
private fun ReplyQuote(content: String, isOwn: Boolean) {
    Row(
        modifier = Modifier
            .widthIn(max = 260.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(
                if (isOwn)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                else
                    MaterialTheme.colorScheme.surface
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(30.dp)
                .clip(MaterialTheme.shapes.extraSmall)
                .background(if (isOwn) Color.White.copy(alpha = 0.6f) else MaterialTheme.colorScheme.primary)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = content,
            style  = MaterialTheme.typography.labelSmall,
            color  = if (isOwn) Color.White.copy(alpha = 0.8f)
            else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}


@Composable
private fun ReactionsRow(
    modifier: Modifier = Modifier,
    reactions: List<Pair<String, Int>>,
    currentDeviceHash: String,
    messageReactions: Map<String, List<String>>,
    onToggle: (String) -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        tonalElevation = 3.dp,
        shadowElevation = 2.dp,
        color = MaterialTheme.colorScheme.background,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            reactions.forEach { (emoji, count) ->

                val hasReacted =
                    messageReactions[emoji]?.contains(currentDeviceHash) == true

                Row(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .then(
                            if (hasReacted) {
                                Modifier.background(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                    shape = CircleShape
                                )
                            } else {
                                Modifier
                            }
                        )
                        .clickable { onToggle(emoji) }
                        .padding(horizontal = 2.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(emoji, fontSize = 14.sp)

                    if (count > 1) {
                        Spacer(Modifier.width(2.dp))

                        Text(
                            count.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color =
                                if (hasReacted)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReplyPreviewBar(content: String, onCancel: () -> Unit) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(32.dp)
                .clip(MaterialTheme.shapes.extraSmall)
                .background(MaterialTheme.colorScheme.background)
        )
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "replying to",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text  = content,
                style    = MaterialTheme.typography.bodySmall,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        IconButton(onClick = onCancel) {
            Icon(
                painter = painterResource(Res.drawable.ic_close),
                contentDescription = "cancel reply",
            )
        }
    }
}

private val commonEmojis = listOf("❤️", "😂", "😮", "😢", "👍", "🔥", "🙏", "💀")

private fun Instant.toRelativeLabel(): String {
    val diff = Clock.System.now() - this
    return when {
        diff < Duration.parse("1m") -> "just now"
        diff < Duration.parse("1h") -> "${diff.inWholeMinutes}m ago"
        diff < Duration.parse("1d") -> "${diff.inWholeHours}h ago"
        else -> "${diff.inWholeDays}d ago"
    }
}
