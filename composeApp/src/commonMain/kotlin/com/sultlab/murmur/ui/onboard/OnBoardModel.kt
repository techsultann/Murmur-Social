package com.sultlab.murmur.ui.onboard

import murmur.composeapp.generated.resources.Res
import murmur.composeapp.generated.resources.murmur_onboard_four
import murmur.composeapp.generated.resources.murmur_onboard_one
import murmur.composeapp.generated.resources.murmur_onboard_three
import murmur.composeapp.generated.resources.murmur_onboard_two
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
        imageRes = Res.drawable.murmur_onboard_one,
        buttonText = "let's go",
        secondaryButtonText = "skip intro"
    ),
    OnBoardModel(
        title = "truly zero identity",
        description = "we don't ask for your name, email, or phone number. ever. your words travel through the space without a face attached.",
        imageRes = Res.drawable.murmur_onboard_two,
        buttonText = "continue",
        secondaryButtonText = "skip"
    ),
    OnBoardModel(
        title = "", // Not used directly in screen 3
        description = "",
        imageRes = Res.drawable.murmur_onboard_three,
        buttonText = "got it",
        secondaryButtonText = "skip"
    ),
    OnBoardModel(
        title = "space",
        description = "your thoughts are yours. no one will ever know.",
        imageRes = Res.drawable.murmur_onboard_four,
        buttonText = "enter the space",
        secondaryButtonText = ""
    )
)