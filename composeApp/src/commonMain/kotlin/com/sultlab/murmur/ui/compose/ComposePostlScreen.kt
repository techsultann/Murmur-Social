package com.sultlab.murmur.ui.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ComposePostScreen(
    viewModel: ComposePostViewModel,
    onDismiss: () -> Unit,
    onPostSuccess: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }


    LaunchedEffect(uiState.submitted) {
        if (uiState.submitted) {
            onPostSuccess()
            onDismiss()
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            ComposeTopBar(
                canSubmit = uiState.canSubmit,
                isSubmitting = uiState.isSubmitting,
                onCancel = onDismiss,
                onPost = viewModel::submit,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(20.dp),
            ) {
                if (uiState.content.isEmpty()) {
                    Text(
                        text  = "what's on your mind? no one will know it's you.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    )
                }

                BasicTextField(
                    value = uiState.content,
                    onValueChange = viewModel::onContentChange,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onBackground,
                    ),
                    modifier = Modifier
                        .fillMaxSize()
                        .focusRequester(focusRequester),
                )
            }

            HorizontalDivider(thickness = 0.5.dp)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Allow comments toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text  = "allow comments",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text  = "others can reply to this post",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Switch(
                        checked = uiState.allowComments,
                        onCheckedChange = viewModel::onAllowCommentsToggle,
                    )
                }

                // Char count + privacy note
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "your device is never stored",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text  = "${uiState.charCount} / 500",
                        style = MaterialTheme.typography.labelSmall,
                        color = when {
                            uiState.isOverLimit -> MaterialTheme.colorScheme.error
                            uiState.charCount > 400 -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }

                // Error message
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
}

@Composable
private fun ComposeTopBar(
    canSubmit: Boolean,
    isSubmitting: Boolean,
    onCancel: () -> Unit,
    onPost: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(onClick = onCancel) {
            Text("cancel")
        }

        Spacer(Modifier.weight(1f))

        Surface(
            shape  = MaterialTheme.shapes.extraLarge,
            color  = MaterialTheme.colorScheme.primary,
            modifier = Modifier,
        ) {
            Text(
                text  = "anonymous",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
            )
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick  = onPost,
            enabled  = canSubmit,
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    modifier    = Modifier.size(14.dp),
                    strokeWidth = 2.dp,
                    color       = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Text("post")
            }
        }
    }
}
