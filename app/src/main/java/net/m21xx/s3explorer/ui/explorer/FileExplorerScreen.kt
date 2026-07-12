package net.m21xx.s3explorer.ui.explorer

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarViewDay
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewCozy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileExplorerScreen(
    viewModel: FileExplorerViewModel = hiltViewModel(),
    onOpenDrawer: () -> Unit,
    onNavigateToConnections: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val pagingItems = viewModel.pagedObjects.collectAsLazyPagingItems()
    val snackbarHostState = remember { SnackbarHostState() }
    val gridState = rememberLazyGridState()

    BackHandler(enabled = uiState.currentPrefix.isNotEmpty()) {
        viewModel.navigateUp()
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            // Only show snackbar if there are already items rendered (not empty)
            if (pagingItems.itemCount > 0) {
                val result = snackbarHostState.showSnackbar(
                    message = error,
                    actionLabel = "Retry",
                    duration = SnackbarDuration.Long
                )
                if (result == SnackbarResult.ActionPerformed) {
                    viewModel.syncCurrentDirectory()
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { 
                    Breadcrumbs(
                        currentPrefix = uiState.currentPrefix,
                        onNavigate = { prefix -> viewModel.navigateToFolder(prefix) }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    val viewModeIcon = when (uiState.viewMode) {
                        ExplorerViewMode.DETAILED_LIST -> Icons.Default.ViewAgenda
                        ExplorerViewMode.COMPACT_LIST -> Icons.Default.List
                        ExplorerViewMode.GALLERY_SMALL -> Icons.Default.ViewCozy
                        ExplorerViewMode.GALLERY_LARGE -> Icons.Default.CalendarViewDay
                    }
                    IconButton(onClick = { viewModel.toggleViewMode() }) {
                        Icon(viewModeIcon, contentDescription = "Toggle View Mode")
                    }
                    IconButton(onClick = onNavigateToConnections) {
                        Icon(Icons.Default.Group, contentDescription = "Connections")
                    }
                    IconButton(onClick = { /* TODO: Add file/folder */ }) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (pagingItems.loadState.refresh is LoadState.Loading || uiState.isSyncing) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            if (uiState.errorMessage != null && pagingItems.itemCount == 0 && !uiState.isSyncing) {
                ErrorState(
                    message = uiState.errorMessage!!,
                    onRetry = { viewModel.syncCurrentDirectory() }
                )
            } else if (pagingItems.loadState.refresh is LoadState.NotLoading && pagingItems.itemCount == 0 && !uiState.isSyncing) {
                EmptyDirectoryState()
            } else {
                val gridCells = when (uiState.viewMode) {
                    ExplorerViewMode.DETAILED_LIST,
                    ExplorerViewMode.COMPACT_LIST,
                    ExplorerViewMode.GALLERY_LARGE -> GridCells.Fixed(1)
                    ExplorerViewMode.GALLERY_SMALL -> GridCells.Fixed(2)
                }

                LazyVerticalGrid(
                    columns = gridCells,
                    state = gridState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp) // Add padding for FAB or scrolling
                ) {
                    items(
                        count = pagingItems.itemCount,
                        key = pagingItems.itemKey { it.objectKey },
                        contentType = pagingItems.itemContentType { it.isDirectory }
                    ) { index ->
                        val item = pagingItems[index]
                        if (item != null) {
                            if (item.isDirectory) {
                                when (uiState.viewMode) {
                                    ExplorerViewMode.DETAILED_LIST,
                                    ExplorerViewMode.COMPACT_LIST -> FolderItem(
                                        item = item,
                                        onClick = { viewModel.navigateToFolder(item.objectKey) }
                                    )
                                    ExplorerViewMode.GALLERY_SMALL,
                                    ExplorerViewMode.GALLERY_LARGE -> GalleryFolderCardItem(
                                        item = item,
                                        onClick = { viewModel.navigateToFolder(item.objectKey) }
                                    )
                                }
                            } else {
                                when (uiState.viewMode) {
                                    ExplorerViewMode.DETAILED_LIST -> DetailedListItem(
                                        item = item, 
                                        getThumbnailUrl = { viewModel.getThumbnailUrl(it) },
                                        getThumbnailUrlSync = { viewModel.getThumbnailUrlSync(it) },
                                        onClick = {}
                                    )
                                    ExplorerViewMode.COMPACT_LIST -> CompactListItem(
                                        item = item, 
                                        getThumbnailUrl = { viewModel.getThumbnailUrl(it) },
                                        getThumbnailUrlSync = { viewModel.getThumbnailUrlSync(it) },
                                        onClick = {}
                                    )
                                    ExplorerViewMode.GALLERY_SMALL,
                                    ExplorerViewMode.GALLERY_LARGE -> GalleryCardItem(
                                        item = item, 
                                        getThumbnailUrl = { viewModel.getThumbnailUrl(it) },
                                        getThumbnailUrlSync = { viewModel.getThumbnailUrlSync(it) },
                                        onClick = {}
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
