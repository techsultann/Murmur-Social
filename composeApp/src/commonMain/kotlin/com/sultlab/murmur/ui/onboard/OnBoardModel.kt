package com.sultlab.murmur.ui.onboard

import murmur.composeapp.generated.resources.Res
import murmur.composeapp.generated.resources.onboarding_1_welcome
import murmur.composeapp.generated.resources.onboarding_2_identity
import murmur.composeapp.generated.resources.icon
import org.jetbrains.compose.resources.DrawableResource

data class OnBoardModel(
    val imageRes: DrawableResource,
    val title: String,
    val description: String,
    val buttonText: String,
    val secondaryButtonText: String = "skip"
)

val onBoardModel = listOf(
    OnBoardModel(
        title = "say what you actually think",
        description = "MURMUR is a place for your unfiltered thoughts. no account, no name, no profile. just words floating in the space and people who get it.",
        imageRes = Res.drawable.onboarding_1_welcome,
        buttonText = "let's go",
        secondaryButtonText = "skip intro"
    ),
    OnBoardModel(
        title = "truly zero identity",
        description = "we don't ask for your name, email, or phone number. ever. your words travel through the space without a face attached.",
        imageRes = Res.drawable.onboarding_2_identity,
        buttonText = "continue",
        secondaryButtonText = "skip"
    ),
    OnBoardModel(
        title = "", // Not used directly in screen 3
        description = "",
        imageRes = Res.drawable.onboarding_2_identity,
        buttonText = "got it",
        secondaryButtonText = "skip"
    ),
    OnBoardModel(
        title = "voidspace",
        description = "your thoughts are yours. no one will ever know.",
        imageRes = Res.drawable.icon,
        buttonText = "enter the void",
        secondaryButtonText = ""
    )
)