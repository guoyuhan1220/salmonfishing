package com.example.salmontrollingassistant.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// MARK: - Large Touch Target Button
@Composable
fun LargeTouchButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clickable(enabled = enabled, onClick = onClick)
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp) // Material Design recommended touch target size
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

// MARK: - One-Handed Operation Mode
@Composable
fun OneHandedModeContainer(
    isOneHandedModeEnabled: Boolean,
    content: @Composable () -> Unit
) {
    var isRightHanded by remember { mutableStateOf(true) }
    
    Box(modifier = Modifier.fillMaxSize()) {
        if (isOneHandedModeEnabled) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(0.85f)
                            .align(if (isRightHanded) Alignment.CenterEnd else Alignment.CenterStart)
                    ) {
                        content()
                    }
                }
                
                // Hand preference toggle
                Card(
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .align(Alignment.CenterHorizontally),
                    shape = RoundedCornerShape(24.dp),
                    elevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Left",
                            color = if (!isRightHanded) MaterialTheme.colors.primary else Color.Gray,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        
                        Switch(
                            checked = isRightHanded,
                            onCheckedChange = { isRightHanded = it }
                        )
                        
                        Text(
                            text = "Right",
                            color = if (isRightHanded) MaterialTheme.colors.primary else Color.Gray,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }
        } else {
            content()
        }
    }
}

// MARK: - Responsive Layout
@Composable
fun ResponsiveLayout(
    content: @Composable (isLandscape: Boolean) -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = if (isLandscape) {
                Modifier
                    .fillMaxHeight()
                    .width(800.dp)
                    .padding(horizontal = 20.dp)
            } else {
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            }
        ) {
            content(isLandscape)
        }
    }
}

// MARK: - Progressive Disclosure
@Composable
fun ProgressiveDisclosurePanel(
    title: String,
    summary: String,
    content: @Composable () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = 4.dp
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.h6
                    )
                    
                    if (!isExpanded) {
                        Text(
                            text = summary,
                            style = MaterialTheme.typography.caption,
                            color = Color.Gray
                        )
                    }
                }
                
                Icon(
                    imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    modifier = Modifier.rotate(rotationState)
                )
            }
            
            // Content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                ) {
                    content()
                }
            }
        }
    }
}

// MARK: - Bottom Action Bar for One-Handed Use
@Composable
fun BottomActionBar(
    actions: List<Pair<String, () -> Unit>>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = 8.dp,
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .height(56.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            actions.forEach { (label, action) ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(onClick = action),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colors.primary
                    )
                }
            }
        }
    }
}

// MARK: - Thumb-Reachable FAB
@Composable
fun ThumbReachableFAB(
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    isRightHanded: Boolean = true
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = if (isRightHanded) Alignment.BottomEnd else Alignment.BottomStart
    ) {
        FloatingActionButton(
            onClick = onClick,
            modifier = Modifier.size(56.dp)
        ) {
            icon()
        }
    }
}