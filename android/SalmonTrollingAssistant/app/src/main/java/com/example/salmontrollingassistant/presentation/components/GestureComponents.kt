package com.example.salmontrollingassistant.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

// MARK: - Pull to Refresh
@Composable
fun PullToRefreshContainer(
    onRefresh: () -> Unit,
    refreshing: Boolean,
    content: @Composable () -> Unit
) {
    val refreshDistance = with(LocalDensity.current) { 80.dp.toPx() }
    var offsetY by remember { mutableStateOf(0f) }
    val pullProgress = (offsetY / refreshDistance).coerceIn(0f, 1f)
    val scope = rememberCoroutineScope()
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Content with pull gesture
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = offsetY.dp)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { },
                        onDragEnd = {
                            if (pullProgress >= 1f && !refreshing) {
                                onRefresh()
                            }
                            scope.launch {
                                offsetY = 0f
                            }
                        },
                        onDragCancel = {
                            scope.launch {
                                offsetY = 0f
                            }
                        },
                        onDrag = { change, dragAmount ->
                            change.consumeAllChanges()
                            if (dragAmount.y > 0 || offsetY > 0) {
                                // Apply resistance to make the pull feel natural
                                offsetY += dragAmount.y * 0.5f
                            }
                        }
                    )
                }
        ) {
            content()
        }
        
        // Pull indicator
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
                .height(40.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (refreshing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(30.dp),
                    color = MaterialTheme.colors.primary
                )
            } else if (offsetY > 0) {
                val rotation by animateFloatAsState(targetValue = pullProgress * 360f)
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Pull to refresh",
                    modifier = Modifier
                        .size(30.dp)
                        .graphicsLayer {
                            rotationZ = rotation
                            alpha = pullProgress
                        },
                    tint = MaterialTheme.colors.primary
                )
            }
        }
    }
}

// MARK: - Swipe Navigation
@Composable
fun SwipeNavigation(
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    when {
                        dragAmount < -50 -> onSwipeLeft()
                        dragAmount > 50 -> onSwipeRight()
                    }
                }
            }
    ) {
        content()
    }
}

// MARK: - Pinch to Zoom
@Composable
fun PinchToZoom(
    content: @Composable () -> Unit
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var rotation by remember { mutableStateOf(0f) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .transformable(
                state = rememberTransformableState { zoomChange, offsetChange, rotationChange ->
                    scale = (scale * zoomChange).coerceIn(1f, 3f)
                    
                    // Only allow offset when zoomed in
                    if (scale > 1f) {
                        offset += offsetChange
                    }
                    
                    rotation += rotationChange
                }
            )
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offset.x,
                translationY = offset.y,
                rotationZ = rotation
            )
    ) {
        content()
    }
}

// MARK: - Double Tap to Zoom
@Composable
fun DoubleTapToZoom(
    content: @Composable () -> Unit
) {
    var scale by remember { mutableStateOf(1f) }
    val scope = rememberCoroutineScope()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        scope.launch {
                            scale = if (scale == 1f) 2f else 1f
                        }
                    }
                )
            }
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale
            )
    ) {
        content()
    }
}

// MARK: - Horizontal Drag Gesture Detection
suspend fun PointerInputScope.detectHorizontalDragGestures(
    onDragEnd: () -> Unit = { },
    onDragStart: (Offset) -> Unit = { },
    onDrag: (change: PointerInputChange, dragAmount: Float) -> Unit
) {
    forEachGesture {
        awaitPointerEventScope {
            val down = awaitFirstDown(requireUnconsumed = false)
            onDragStart(down.position)
            
            var dragDistance = 0f
            var overallDistance = Offset.Zero
            
            do {
                val event = awaitPointerEvent()
                val dragEvent = event.changes.firstOrNull()
                
                if (dragEvent != null && dragEvent.positionChanged()) {
                    val horizontalDragAmount = dragEvent.position.x - dragEvent.previousPosition.x
                    dragDistance = horizontalDragAmount
                    overallDistance += Offset(horizontalDragAmount, 0f)
                    
                    // Only trigger the onDrag callback when we've moved a significant distance
                    if (abs(overallDistance.x) > 50) {
                        onDrag(dragEvent, overallDistance.x)
                        overallDistance = Offset.Zero
                    }
                    
                    dragEvent.consumePositionChange()
                }
            } while (event.changes.any { it.pressed })
            
            onDragEnd()
        }
    }
}