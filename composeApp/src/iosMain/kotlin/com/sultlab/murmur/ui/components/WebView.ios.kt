package com.sultlab.murmur.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import platform.WebKit.WKWebView
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cValue
import platform.CoreGraphics.CGRect

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun MurmurWebView(
    url: String,
    modifier: Modifier,
) {
    UIKitView(
        factory = {
            WKWebView(frame = cValue<CGRect> {}).apply {
                loadRequest(NSURLRequest(uRL = NSURL(string = url)))
            }
        },
        modifier = modifier,
        update = { webView ->
            webView.loadRequest(NSURLRequest(uRL = NSURL(string = url)))
        },
    )
}
