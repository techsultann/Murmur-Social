package com.sultlab.murmur.ui.onboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sultlab.murmur.ui.components.AnonymousTag
import androidx.compose.ui.text.style.TextDecoration
import com.sultlab.murmur.ui.theme.Accent
import murmur.composeapp.generated.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun OnboardItem(
    page: OnBoardModel,
    pageCount: Int,
    currentPage: Int,
    onClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(page.imageRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                when (currentPage) {
                    0 -> OnBoarding1Item(page)
                    1 -> Onboarding2Item(page)
                    2 -> Onboarding3Item()
                    3 -> Onboarding4Item(page)
                }
            }

            // Pager Indicator
            Row(
                Modifier
                    .wrapContentHeight()
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pageCount) { iteration ->
                    val isSelected = currentPage == iteration
                    val color =
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    Box(
                        modifier = Modifier
                            .padding(2.dp)
                            .width(if (isSelected) 18.dp else 8.dp)
                            .height(8.dp)
                            .background(
                                color = color,
                                shape = CircleShape
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().navigationBarsPadding()
            ) {
                Button(
                    onClick = onClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = page.buttonText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (page.secondaryButtonText.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = page.secondaryButtonText,
                        modifier = Modifier
                            .clickable { /* Handle skip */ }
                            .padding(8.dp),
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 16.sp,
                        style = MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.Underline)
                    )
                }

                if (currentPage == 3) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = buildAnnotatedString {
                            append("by continuing you accept our ")
                            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                                append("content policy")
                            }
                        },
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun OnBoarding1Item(page: OnBoardModel) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = buildAnnotatedString {
                val words = page.title.split(" ")
                if (words.size >= 4) {
                    append(words.take(words.size - 2).joinToString(" ") + "\n")
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                        append(words.drop(words.size - 2).joinToString(" "))
                    }
                } else {
                    append(page.title)
                }
            },
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            lineHeight = 40.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge.copy(
                shadow = Shadow(
                    color = Accent,
                    offset = Offset(1f, 1f),
                    blurRadius = 3f
                )
            ),
            color = Color.White,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
        )
        Spacer(modifier = Modifier.height(150.dp))
    }
}

@Composable
fun Onboarding2Item(page: OnBoardModel) {
    val tags = listOf("no account needed", "no email", "no tracking", "no profile")
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = buildAnnotatedString {
                append("truly ")
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                    append("zero")
                }
                append("\nidentity")
            },
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            lineHeight = 40.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Tags(tags = tags)
    }
}

@Composable
fun Onboarding3Item() {
    val howItWorks = listOf(
        OnboardDetailsItem(
            icon = Res.drawable.add,
            title = "write anything",
            details = "up to 500 characters, no context needed"
        ),
        OnboardDetailsItem(
            icon = Res.drawable.circle,
            title = "float into the space",
            details = "your post appears instantly, no moderation queue"
        ),
        OnboardDetailsItem(
            icon = Res.drawable.favorite_outline,
            title = "others connect",
            details = "likes and replies. everyone stays nameless"
        ),
        OnboardDetailsItem(
            icon = Res.drawable.comment_16,
            title = "you choose or allow replies",
            details = "toggle comments on or off for each post"
        ),
    )
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        howItWorks.forEach { detail ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                color = Color.White.copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .border(
                                width = 1.dp,
                                color = Color.White.copy(alpha = 0.2f),
                                shape = CircleShape
                            )
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(detail.icon),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = detail.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Text(
                            text = detail.details,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Onboarding4Item(page: OnBoardModel) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = buildAnnotatedString {
                append("MUR")
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                    append("MUR")
                }
            },
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(250.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OnboardingCheckItem("no sign-up, ever")
            OnboardingCheckItem("zero personal data collected")
            OnboardingCheckItem("you control comments on each post")

        }
    }
}


@Composable
fun OnboardingCheckItem(text: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(0.85f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        color = Color.White.copy(alpha = 0.05f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Tags(tags: List<String>) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tags.forEach { tag ->
            AnonymousTag(text = tag)
        }
    }
}

data class OnboardDetailsItem(
    val icon: DrawableResource,
    val title: String,
    val details: String
)
