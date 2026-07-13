package net.m21xx.s3explorer.ui.viewer

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.text.TextUtils
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.ScreenLockRotation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

class VideoPlayerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Make activity fullscreen / immersive
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
        insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        val url = intent.getStringExtra("url") ?: return finish()
        val title = intent.getStringExtra("title") ?: "Video"

        setContent {
            MaterialTheme {
                VideoPlayerScreen(url = url, title = title, onBack = { finish() })
            }
        }
    }
}

@Composable
fun VideoPlayerScreen(url: String, title: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(url))
            prepare()
            playWhenReady = true
        }
    }

    var isPlaying by remember { mutableStateOf(exoPlayer.isPlaying) }
    var currentPosition by remember { mutableStateOf(0L) }
    var duration by remember { mutableStateOf(0L) }
    var showControls by remember { mutableStateOf(true) }
    var isFullscreen by remember { mutableStateOf(activity?.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) }
    
    var isSeeking by remember { mutableStateOf(false) }
    var isLooping by remember { mutableStateOf(false) }
    var isRotationLocked by remember { mutableStateOf(false) }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    duration = exoPlayer.duration.coerceAtLeast(0L)
                }
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    // Auto-hide controls after 3 seconds of playing
    LaunchedEffect(isPlaying, showControls, isSeeking) {
        if (isPlaying && showControls && !isSeeking) {
            delay(3000L)
            showControls = false
        }
    }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            if (!isSeeking) {
                currentPosition = exoPlayer.currentPosition
            }
            delay(50L) // Poll every 50ms for smooth progress bar
        }
    }

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        if (dragAmount > 50f) { // Swipe down threshold
                            onBack()
                        }
                    }
                )
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                showControls = !showControls
            }
    ) {
        // Video Surface
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    useController = false
                    player = exoPlayer
                    layoutParams = android.view.ViewGroup.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300)),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                
                // Top Bar
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(top = 32.dp, bottom = 8.dp, start = 8.dp, end = 8.dp)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    
                    Text(
                        text = title,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 48.dp)
                    )
                }

                // Center Play/Pause button
                IconButton(
                    onClick = {
                        if (isPlaying) {
                            exoPlayer.pause()
                        } else {
                            if (exoPlayer.playbackState == Player.STATE_ENDED) {
                                exoPlayer.seekTo(0)
                            }
                            exoPlayer.play()
                        }
                        showControls = true
                    },
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(64.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(64.dp)
                    )
                }

                // Bottom Panel
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(horizontal = 16.dp, vertical = 24.dp)
                ) {
                    // Progress Bar (Slider)
                    Slider(
                        value = if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f,
                        onValueChange = { percent ->
                            isSeeking = true
                            currentPosition = (percent * duration).toLong()
                            exoPlayer.seekTo(currentPosition)
                        },
                        onValueChangeFinished = {
                            isSeeking = false
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = Color.Red,
                            activeTrackColor = Color.Red,
                            inactiveTrackColor = Color.Gray
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Timecode
                        Text(
                            text = "${formatTime(currentPosition)} / ${formatTime(duration)}",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Rotation Lock Toggle
                            IconButton(
                                onClick = {
                                    if (activity != null) {
                                        isRotationLocked = !isRotationLocked
                                        activity.requestedOrientation = if (isRotationLocked) {
                                            ActivityInfo.SCREEN_ORIENTATION_LOCKED
                                        } else {
                                            ActivityInfo.SCREEN_ORIENTATION_SENSOR
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (isRotationLocked) Icons.Default.ScreenLockRotation else Icons.Default.ScreenRotation,
                                    contentDescription = "Rotation Lock",
                                    tint = if (isRotationLocked) Color.Red else Color.White
                                )
                            }
                            
                            // Loop Toggle
                            IconButton(
                                onClick = {
                                    isLooping = !isLooping
                                    exoPlayer.repeatMode = if (isLooping) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Repeat,
                                    contentDescription = "Loop Toggle",
                                    tint = if (isLooping) Color.Red else Color.White
                                )
                            }

                            // Fullscreen Toggle
                            IconButton(
                                onClick = {
                                    if (activity != null) {
                                        if (isFullscreen) {
                                            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                                            isFullscreen = false
                                        } else {
                                            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                                            isFullscreen = true
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                    contentDescription = "Fullscreen Toggle",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(ms)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
