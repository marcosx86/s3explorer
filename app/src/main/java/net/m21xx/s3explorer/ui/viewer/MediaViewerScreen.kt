package net.m21xx.s3explorer.ui.viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.decode.VideoFrameDecoder
import androidx.compose.runtime.DisposableEffect
import kotlinx.coroutines.launch


@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun MediaViewerScreen(
    onNavigateBack: () -> Unit,
    viewModel: MediaViewerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val view = LocalView.current
    val window = (view.context as? android.app.Activity)?.window

    // Parametrize full screen hiding system UI to be activated later
    val isImmersiveModeEnabled = false // TODO: Wire up to SettingsDataStore later

    DisposableEffect(isImmersiveModeEnabled) {
        val insetsController = window?.let { WindowCompat.getInsetsController(it, view) }
        if (isImmersiveModeEnabled) {
            insetsController?.hide(WindowInsetsCompat.Type.systemBars())
            insetsController?.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        onDispose {
            insetsController?.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.White)
            }
        } else if (uiState.mediaItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No media found", color = Color.White)
            }
        } else {
            val pagerState = rememberPagerState(
                initialPage = uiState.initialPage,
                pageCount = { uiState.mediaItems.size }
            )
            val coroutineScope = rememberCoroutineScope()

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                key = { page -> uiState.mediaItems[page].entity.objectKey }
            ) { page ->
                val item = uiState.mediaItems[page]
                MediaPage(
                    item = item,
                    getPresignedUrl = { viewModel.getPresignedUrl(it) },
                    onLeftBorderTap = {
                        if (pagerState.currentPage > 0) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        }
                    },
                    onRightBorderTap = {
                        if (pagerState.currentPage < pagerState.pageCount - 1) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                    onSwipeDown = onNavigateBack
                )
            }
        }

        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 32.dp, start = 8.dp)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
        }
    }
}

@Composable
fun MediaPage(
    item: MediaItem,
    getPresignedUrl: suspend (String) -> String?,
    onLeftBorderTap: () -> Unit,
    onRightBorderTap: () -> Unit,
    onSwipeDown: () -> Unit
) {
    var url by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(item.entity.objectKey) {
        url = getPresignedUrl(item.entity.objectKey)
    }

    ZoomableMediaBox(
        modifier = Modifier.fillMaxSize(),
        isZoomable = !item.isVideo,
        onLeftBorderTap = onLeftBorderTap,
        onRightBorderTap = onRightBorderTap,
        onSwipeDown = onSwipeDown
    ) {
        if (url != null) {
            val context = LocalContext.current
            val model = remember(url, item.isVideo) {
                val builder = ImageRequest.Builder(context)
                    .data(url)
                    .crossfade(true)
                if (item.isVideo) {
                    builder.decoderFactory { result, options, _ ->
                        VideoFrameDecoder(result.source, options)
                    }
                }
                builder.build()
            }
            
            AsyncImage(
                model = model,
                contentDescription = item.entity.objectKey,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )

            if (item.isVideo) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play Video",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier
                        .size(64.dp)
                        .align(Alignment.Center)
                )
            }
        } else {
            CircularProgressIndicator(color = Color.White)
        }
    }
}
