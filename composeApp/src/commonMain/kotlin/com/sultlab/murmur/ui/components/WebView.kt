package com.sultlab.murmur.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun MurmurWebView(
    url: String,
    modifier: Modifier = Modifier
)
