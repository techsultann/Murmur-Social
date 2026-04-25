package com.sultlab.murmur.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sultlab.murmur.data.model.Post
import com.sultlab.murmur.ui.theme.Liked
import murmur.composeapp.generated.resources.Res
import murmur.composeapp.generated.resources.comment_16
import murmur.composeapp.generated.resources.favorite_filled
import murmur.composeapp.generated.resources.favorite_outline
import org.jetbrains.compose.resources.painterResource
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

@Composable
fun AnonymousPost(
    post: Post,
    onCommentsClick: () -> Unit = {},
    onReportClick: () -> Unit = {},
    onLikeClick: () -> Unit = {},
    onPostClick: () -> Unit = {}
){
    Column(
        modifier = Modifier
            .clickable{
                onPostClick()
            }
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnonymousTag()

            Text(
                text = "11m ago",
                style = MaterialTheme.typography.labelMedium
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = post.content,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ActionIcon(
                painter = painterResource(Res.drawable.favorite_outline),
                contentDescription = "Like",
                count = 123,
                onClick = onLikeClick
            )
            ActionIcon(
                painter = painterResource(Res.drawable.comment_16),
                contentDescription = "Comment",
                count = 22,
                onClick = onCommentsClick
            )

        }
    }
}

@Composable
fun PostCard(
    post: Post,
    onClick: () -> Unit,
    onLikeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
    ) {
        // ── Meta row ──────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AnonymousTag()
            Spacer(Modifier.weight(1f))
            Text(
                text  = post.createdAt.toRelativeString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(Modifier.height(8.dp))

        // ── Content ───────────────────────────────────────────
        Text(
            text = post.content,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(10.dp))

        // ── Actions row ───────────────────────────────────────
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Like
            LikeButton(
                count     = post.likeCount,
                liked     = post.likedByMe,
                onClick   = onLikeClick,
            )

            Spacer(Modifier.width(16.dp))

            // Comment count or "comments off" badge
            if (post.allowComments) {
                CommentCountChip(count = post.commentCount)
            } else {
                CommentsOffChip()
            }
        }
    }
}
@Composable
private fun CommentCountChip(count: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            painter = painterResource(Res.drawable.comment_16),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text  = count.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun LikeButton(
    count: Int,
    liked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tint = if (liked) Liked else MaterialTheme.colorScheme.onSurfaceVariant
    val scale by animateFloatAsState(
        targetValue = if (liked) 1.2f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "heart_scale",
    )

    Row(
        modifier          = modifier.clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            painter = if (liked) painterResource(Res.drawable.favorite_filled) else painterResource(Res.drawable.favorite_outline),
            contentDescription = if (liked) "unlike" else "like",
            tint  = tint,
            modifier  = Modifier.size(20.dp).scale(scale),
        )
        Text(
            text  = count.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = tint,
        )
    }
}

@Composable
fun ActionIcon(
    painter: Painter,
    contentDescription: String,
    count: Int?,
    onClick: () -> Unit
){
    Row(
        modifier = Modifier
            .clickable(
                onClick = onClick,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ),
        verticalAlignment = Alignment.CenterVertically
    ){
        Icon(
            painter = painter,
            contentDescription = contentDescription,
            modifier = Modifier
                .size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        if (count != null){
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun CommentsOffChip() {
    Text(
        text  = "comments off",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
    )
}

private fun Instant.toRelativeString(): String {
    val diff = Clock.System.now() - this
    return when {
        diff < 1.minutes  -> "just now"
        diff < 1.hours    -> "${diff.inWholeMinutes}m ago"
        diff < 1.days     -> "${diff.inWholeHours}h ago"
        else              -> "${diff.inWholeDays}d ago"
    }
}
