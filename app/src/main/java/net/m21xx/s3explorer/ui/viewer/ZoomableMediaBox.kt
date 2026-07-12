package net.m21xx.s3explorer.ui.viewer

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import kotlinx.coroutines.launch

@Composable
fun ZoomableMediaBox(
    modifier: Modifier = Modifier,
    isZoomable: Boolean = true,
    onLeftBorderTap: (() -> Unit)? = null,
    onRightBorderTap: (() -> Unit)? = null,
    onSwipeDown: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    
    val animatedScale = remember { Animatable(1f) }
    val animatedOffsetX = remember { Animatable(0f) }
    val animatedOffsetY = remember { Animatable(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .pointerInput(isZoomable) {
                val touchSlop = viewConfiguration.touchSlop
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    var totalPan = Offset.Zero
                    var isDragging = false
                    
                    do {
                        val event = awaitPointerEvent()
                        val zoom = event.calculateZoom()
                        val pan = event.calculatePan()
                        
                        val newScale = if (isZoomable) (scale * zoom).coerceIn(1f, 10f) else 1f
                        val isZooming = isZoomable && event.changes.size > 1 && zoom != 1f
                        
                        if (isZooming) {
                            scale = newScale
                        }
                        
                        if (scale > 1f) {
                            totalPan += pan
                            if (isZooming || isDragging || totalPan.getDistance() > touchSlop) {
                                isDragging = true
                                val maxX = (size.width * (scale - 1)) / 2f
                                val maxY = (size.height * (scale - 1)) / 2f
                                val newOffsetX = (offset.x + pan.x).coerceIn(-maxX, maxX)
                                val newOffsetY = (offset.y + pan.y).coerceIn(-maxY, maxY)
                                offset = Offset(newOffsetX, newOffsetY)
                                
                                event.changes.forEach {
                                    if (it.positionChanged()) {
                                        it.consume()
                                    }
                                }
                            }
                        } else {
                            offset = Offset.Zero
                            totalPan += pan
                            if (isZooming) {
                                event.changes.forEach {
                                    if (it.positionChanged()) {
                                        it.consume()
                                    }
                                }
                            }
                        }
                    } while (event.changes.any { it.pressed })

                    if (scale == 1f) {
                        val threshold = touchSlop * 3
                        if (totalPan.y > threshold && Math.abs(totalPan.y) > Math.abs(totalPan.x)) {
                            onSwipeDown?.invoke()
                        }
                    }
                }
            }
            .pointerInput(isZoomable) {
                detectTapGestures(
                    onDoubleTap = { tapOffset ->
                        if (isZoomable) {
                            coroutineScope.launch {
                                if (scale > 1f) {
                                    val currentScale = scale
                                    val currentOffset = offset
                                    scale = 1f
                                    offset = Offset.Zero
                                    
                                    launch {
                                        animatedOffsetX.snapTo(currentOffset.x)
                                        animatedOffsetX.animateTo(0f, tween(300))
                                    }
                                    launch {
                                        animatedOffsetY.snapTo(currentOffset.y)
                                        animatedOffsetY.animateTo(0f, tween(300))
                                    }
                                    animatedScale.snapTo(currentScale)
                                    animatedScale.animateTo(1f, tween(300))
                                } else {
                                    scale = 4f
                                    val maxX = (size.width * (scale - 1)) / 2f
                                    val maxY = (size.height * (scale - 1)) / 2f
                                    val centerX = size.width / 2f
                                    val centerY = size.height / 2f
                                    val targetX = ((centerX - tapOffset.x) * scale).coerceIn(-maxX, maxX)
                                    val targetY = ((centerY - tapOffset.y) * scale).coerceIn(-maxY, maxY)
                                    offset = Offset(targetX, targetY)
                                    
                                    launch {
                                        animatedOffsetX.snapTo(0f)
                                        animatedOffsetX.animateTo(targetX, tween(300))
                                    }
                                    launch {
                                        animatedOffsetY.snapTo(0f)
                                        animatedOffsetY.animateTo(targetY, tween(300))
                                    }
                                    animatedScale.snapTo(1f)
                                    animatedScale.animateTo(4f, tween(300))
                                }
                            }
                        }
                    },
                    onTap = { tapOffset ->
                        if (scale == 1f) {
                            val borderLimit = size.width * 0.15f
                            if (tapOffset.x < borderLimit) {
                                onLeftBorderTap?.invoke()
                            } else if (tapOffset.x > size.width - borderLimit) {
                                onRightBorderTap?.invoke()
                            }
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.graphicsLayer {
                val isAnimating = animatedScale.isRunning || animatedOffsetX.isRunning || animatedOffsetY.isRunning
                val currentScale = if (isAnimating) animatedScale.value else scale
                val currentOffsetX = if (isAnimating) animatedOffsetX.value else offset.x
                val currentOffsetY = if (isAnimating) animatedOffsetY.value else offset.y
                
                scaleX = currentScale
                scaleY = currentScale
                translationX = currentOffsetX
                translationY = currentOffsetY
            },
            content = content
        )
    }
}
