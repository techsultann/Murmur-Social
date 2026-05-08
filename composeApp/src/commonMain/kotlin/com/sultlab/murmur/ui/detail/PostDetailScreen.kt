package com.sultlab.murmur.ui.detail

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sultlab.murmur.ui.components.AnonymousTag
import com.sultlab.murmur.ui.components.LikeButton
import com.sultlab.murmur.ui.components.ReportSheet
import murmur.composeapp.generated.resources.Res
import murmur.composeapp.generated.resources.chevron_backward
import murmur.composeapp.generated.resources.outline_flag
import murmur.composeapp.generated.resources.send
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    viewModel: PostDetailViewModel,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
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
                        Text(
                            text  = "${uiState.comments.size} replies",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    actions = {
                        IconButton(onClick = { viewModel.showReportSheet() }) {
                            Icon(painterResource(Res.drawable.outline_flag), contentDescription = "report")
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
                if (uiState.post.allowComments) {
                    CommentInputBar(
                        value = uiState.commentInput,
                        onValueChange = viewModel::onCommentInputChange,
                        onSend = viewModel::sendComment,
                        isSending = uiState.isSendingComment,
                    )
                }
            },
        ) { padding ->
            LazyColumn(
                modifier  = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 16.dp),
            ) {

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AnonymousTag()
                            Spacer(Modifier.weight(1f))
                            Text(
                                text  = "just now",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text  = uiState.post.content,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Spacer(Modifier.height(14.dp))
                        HorizontalDivider(thickness = 0.5.dp)
                        Spacer(Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LikeButton(
                                count   = uiState.post.likeCount,
                                liked   = uiState.post.likedByMe,
                                onClick = viewModel::toggleLike,
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text  = "tap heart to show solidarity",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            )
                        }
                    }
                    HorizontalDivider(thickness = 0.5.dp)
                }

                item {
                    Text(
                        text = "replies",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    )
                }

                if (uiState.isLoadingComments) {
                    item {
                        Box(
                            modifier            = Modifier.fillMaxWidth().padding(24.dp),
                            contentAlignment    = Alignment.Center,
                        ) {
                            CircularProgressIndicator(strokeWidth = 2.dp)
                        }
                    }
                } else {
                    items(uiState.comments, key = { it.id }) { comment ->
                        CommentItem(
                            content   = comment.content,
                            onReport  = { viewModel.showReportSheet(comment.id) },
                        )
                        HorizontalDivider(thickness = 0.5.dp)
                    }
                }
            }
        }


        if (uiState.reportSheetVisible) {
            ReportSheet(
                onDismiss = viewModel::hideReportSheet,
                onReport  = viewModel::submitReport,
            )
        }
    }
}

@Composable
private fun CommentItem(content: String, onReport: () -> Unit) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            AnonymousTag()
            Spacer(Modifier.height(6.dp))
            Text(
                text  = content,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        IconButton(
            onClick  = onReport,
            modifier = Modifier.size(20.dp),
        ) {
            Icon(
                painter = painterResource(Res.drawable.outline_flag),
                contentDescription = "report comment",
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            )
        }
    }
}

@Composable
private fun CommentInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    isSending: Boolean,
) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text("reply anonymously…") },
            singleLine = false,
            maxLines = 3,
            modifier = Modifier.weight(1f),
            shape = MaterialTheme.shapes.extraLarge,
        )
        IconButton(
            onClick  = onSend,
            enabled  = value.isNotBlank() && !isSending,
        ) {
            if (isSending) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            } else {
                Icon(painterResource(Res.drawable.send), contentDescription = "send")
            }
        }
    }
}