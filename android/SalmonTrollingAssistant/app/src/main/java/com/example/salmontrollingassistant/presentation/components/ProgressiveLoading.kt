package com.example.salmontrollingassistant.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * A component that implements progressive loading of content
 * to improve perceived performance and user experience
 */
@Composable
fun <T> ProgressiveLoader(
    data: T?,
    isLoading: Boolean,
    loadingContent: @Composable () -> Unit = { DefaultLoadingIndicator() },
    errorContent: @Composable () -> Unit = { DefaultErrorContent() },
    content: @Composable (T) -> Unit
) {
    var showContent by remember { mutableStateOf(false) }
    var showLoading by remember { mutableStateOf(true) }
    
    // Handle state changes
    LaunchedEffect(data, isLoading) {
        if (isLoading) {
            showLoading = true
            showContent = false
        } else if (data != null) {
            // Short delay before showing content for smoother transition
            delay(300)
            showContent = true
            delay(200)
            showLoading = false
        } else {
            showLoading = false
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Show content with fade-in animation when data is available
        AnimatedVisibility(
            visible = showContent && data != null,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            data?.let { content(it) }
        }
        
        // Show loading indicator with fade-in/out animation
        AnimatedVisibility(
            visible = showLoading,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300)),
            modifier = Modifier.align(Alignment.Center)
        ) {
            loadingContent()
        }
        
        // Show error content if data is null and not loading
        AnimatedVisibility(
            visible = !showLoading && data == null,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300)),
            modifier = Modifier.align(Alignment.Center)
        ) {
            errorContent()
        }
    }
}

/**
 * A component that implements progressive loading of a list of items
 * to improve perceived performance and user experience
 */
@Composable
fun <T> ProgressiveList(
    items: List<T>?,
    isLoading: Boolean,
    loadingContent: @Composable () -> Unit = { DefaultLoadingIndicator() },
    errorContent: @Composable () -> Unit = { DefaultErrorContent() },
    emptyContent: @Composable () -> Unit = { DefaultEmptyContent() },
    itemContent: @Composable (T) -> Unit
) {
    var visibleItems by remember { mutableStateOf<List<T>>(emptyList()) }
    var showLoading by remember { mutableStateOf(true) }
    
    // Handle state changes
    LaunchedEffect(items, isLoading) {
        if (isLoading) {
            showLoading = true
            visibleItems = emptyList()
        } else if (items != null) {
            if (items.isEmpty()) {
                showLoading = false
                visibleItems = emptyList()
            } else {
                // Progressive loading of items
                showLoading = false
                
                // For small lists, load all at once
                if (items.size <= 5) {
                    visibleItems = items
                } else {
                    // For larger lists, load in batches
                    val initialBatch = items.take(5)
                    visibleItems = initialBatch
                    
                    // Load remaining items in batches
                    val remainingItems = items.drop(5)
                    val batchSize = 5
                    
                    for (i in remainingItems.indices step batchSize) {
                        val endIndex = (i + batchSize).coerceAtMost(remainingItems.size)
                        val batch = remainingItems.subList(i, endIndex)
                        delay(100) // Small delay between batches
                        visibleItems = visibleItems + batch
                    }
                }
            }
        } else {
            showLoading = false
            visibleItems = emptyList()
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Show content with items
        if (visibleItems.isNotEmpty()) {
            androidx.compose.foundation.lazy.LazyColumn {
                items(visibleItems.size) { index ->
                    itemContent(visibleItems[index])
                }
            }
        }
        
        // Show empty content if items list is empty and not loading
        AnimatedVisibility(
            visible = !showLoading && visibleItems.isEmpty() && items != null,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300)),
            modifier = Modifier.align(Alignment.Center)
        ) {
            emptyContent()
        }
        
        // Show loading indicator
        AnimatedVisibility(
            visible = showLoading,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300)),
            modifier = Modifier.align(Alignment.Center)
        ) {
            loadingContent()
        }
        
        // Show error content if items is null and not loading
        AnimatedVisibility(
            visible = !showLoading && items == null,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300)),
            modifier = Modifier.align(Alignment.Center)
        ) {
            errorContent()
        }
    }
}

@Composable
fun DefaultLoadingIndicator() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun DefaultErrorContent() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text("Unable to load content. Please try again.")
    }
}

@Composable
fun DefaultEmptyContent() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text("No items to display")
    }
}