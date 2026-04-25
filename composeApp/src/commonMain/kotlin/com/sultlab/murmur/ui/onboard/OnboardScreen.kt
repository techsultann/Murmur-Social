package com.sultlab.murmur.ui.onboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    onGetStartedClick: () -> Unit
){

    val pagerState = rememberPagerState(
        pageCount = { onBoardModel.size }
    )
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        HorizontalPager(
            state = pagerState
        ) { page ->
            OnboardItem(
                page = onBoardModel[page],
                pageCount = pagerState.pageCount,
                currentPage = pagerState.currentPage,
                onClick = {
                    if (page == onBoardModel.lastIndex){
                        onGetStartedClick()
                    } else {
                        coroutineScope.launch { pagerState.animateScrollToPage(page + 1) }
                    }
                }
            )
        }
    }
}