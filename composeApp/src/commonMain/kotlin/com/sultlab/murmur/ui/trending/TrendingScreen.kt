package com.sultlab.murmur.ui.trending

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sultlab.murmur.data.model.Post
import com.sultlab.murmur.ui.components.ActionIcon
import com.sultlab.murmur.ui.components.AnonymousPost
import com.sultlab.murmur.ui.components.EmptyTrendingState
import com.sultlab.murmur.ui.components.MurMurTopBar
import com.sultlab.murmur.ui.theme.Accent
import com.sultlab.murmur.ui.theme.AccentDim
import com.sultlab.murmur.ui.theme.TextPrimary
import murmur.composeapp.generated.resources.Res
import murmur.composeapp.generated.resources.favorite_outline
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrendingScreen(
    viewModel: TrendingViewModel,
    onPostClick: (Post) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            TrendingTopBar(
                selectedWindow = uiState.window,
                onWindowChange = viewModel::setWindow,
            )
        },
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator() }
            }

            uiState.posts.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) { EmptyTrendingState() }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(bottom = 16.dp),
                ) {
                    itemsIndexed(
                        items = uiState.posts,
                        key   = { _, post -> post.id },
                    ) { index, post ->
                        TrendingCard(
                            rank     = index + 1,
                            post     = post,
                            maxLikes = uiState.maxLikes,
                            onClick  = { onPostClick(post) },
                            onLike   = { viewModel.toggleLike(post) },
                        )
                        HorizontalDivider(thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

@Composable
private fun TrendingTopBar(
    selectedWindow: TrendingWindow,
    onWindowChange: (TrendingWindow) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("trending", style = MaterialTheme.typography.titleLarge)
            Text(
                text  = "most resonant in the last ${selectedWindow.label}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TrendingWindow.entries.forEach { window ->
                val selected = window == selectedWindow
                Surface(
                    onClick = { onWindowChange(window) },
                    shape = MaterialTheme.shapes.extraLarge,
                    color = if (selected) AccentDim
                    else MaterialTheme.colorScheme.surfaceVariant,
                    border = if (selected) null
                    else BorderStroke(
                        0.5.dp, MaterialTheme.colorScheme.outline
                    ),
                ) {
                    Text(
                        text = window.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) TextPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun TrendingCard(
    rank: Int,
    post: Post,
    maxLikes: Int,
    onClick: () -> Unit,
    onLike: () -> Unit,
) {
    val isHot = rank == 1

    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Rank
        Text(
            text  = if (isHot) "↑" else rank.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = if (isHot) Accent
            else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(20.dp).padding(top = 2.dp),
        )

        Column(modifier = Modifier.weight(1f)) {
            // Content
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(Modifier.height(8.dp))

            // Stats row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ActionIcon(
                    painter = painterResource(Res.drawable.favorite_outline),
                    contentDescription = "like",
                    count = post.likeCount,
                    onClick = onLike,
                )

                Text(
                    text  = if (post.allowComments) "${post.commentCount}" else "comments off",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(8.dp))

            // Relative popularity bar
            val fraction = if (maxLikes > 0) post.likeCount.toFloat() / maxLikes else 0f
            LinearProgressIndicator(
                progress           = { fraction },
                modifier           = Modifier.fillMaxWidth().height(2.dp),
                color              = Accent.copy(alpha = 0.6f),
                trackColor         = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap          = StrokeCap.Round,
            )
        }
    }
}