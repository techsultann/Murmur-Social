package com.sultlab.murmur.ui.compose

import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sultlab.murmur.data.model.Post
import com.sultlab.murmur.ui.components.AnonymousTag
import com.sultlab.murmur.ui.theme.Accent
import com.sultlab.murmur.ui.theme.Surface
import murmur.composeapp.generated.resources.Res
import murmur.composeapp.generated.resources.resource_public
import org.jetbrains.compose.resources.painterResource

@Composable
fun PostSuccessScreen(
    post: Post,
    onBackToFeed: () -> Unit,
    onWriteAnother: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {

        VoidRipple()

        Spacer(Modifier.height(28.dp))

        Text(
            text = "your thought\nis in the void",
            style     = MaterialTheme.typography.displaySmall.copy(
                fontSize = 26.sp,
            ),
            textAlign = TextAlign.Center,
            letterSpacing = (-0.5).sp,
        )

        Spacer(Modifier.height(10.dp))

        Text(
            text  = "it's floating out there right now, completely anonymous. someone somewhere will feel less alone because of it.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(24.dp))

        PostPreviewCard(post = post)

        Spacer(Modifier.height(24.dp))

        Button(
            onClick  = onBackToFeed,
            modifier = Modifier.fillMaxWidth(),
            shape    = MaterialTheme.shapes.large,
            colors   = ButtonDefaults.buttonColors(
                containerColor = Accent,
            ),
        ) {
            Text("back to feed")
        }

        Spacer(Modifier.height(12.dp))

        TextButton(
            onClick  = onWriteAnother,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text  = "write another thought",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun VoidRipple() {
    val infiniteTransition = rememberInfiniteTransition(label = "ripple")

    val scales = (0..2).map { index ->
        infiniteTransition.animateFloat(
            initialValue   = 0.7f,
            targetValue    = 1.1f,
            animationSpec  = infiniteRepeatable(
                animation  = tween(2000, easing = EaseOut),
                repeatMode = RepeatMode.Restart,
                initialStartOffset = StartOffset(index * 400),
            ),
            label = "scale_$index",
        )
    }

    val alphas = (0..2).map { index ->
        infiniteTransition.animateFloat(
            initialValue  = 0.4f,
            targetValue   = 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = EaseOut),
                repeatMode = RepeatMode.Restart,
                initialStartOffset = StartOffset(index * 400),
            ),
            label = "alpha_$index",
        )
    }

    Box(
        modifier          = Modifier.size(100.dp),
        contentAlignment  = Alignment.Center,
    ) {
        // Three outward-rippling rings
        scales.forEachIndexed { i, scale ->
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(scale.value),
            ) {
                Surface(
                    shape  = MaterialTheme.shapes.extraLarge,
                    color  = Color.Transparent,
                    border = BorderStroke(
                        width = 1.dp,
                        color = Accent.copy(alpha = alphas[i].value),
                    ),
                    modifier = Modifier.fillMaxSize(),
                ) {}
            }
        }

        // Core icon
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = Accent,
            modifier = Modifier.size(48.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(Res.drawable.resource_public),
                    contentDescription = null,
                    tint  = Color.White,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}

// ── Post preview ──────────────────────────────────────────────

@Composable
private fun PostPreviewCard(post: Post) {
    Surface(
        color    = MaterialTheme.colorScheme.surfaceVariant,
        shape    = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text  = "your post",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text  = "\"${post.content}\"",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AnonymousTag()
                Text(
                    text  = "just now",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}