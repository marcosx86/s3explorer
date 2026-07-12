package net.m21xx.s3explorer.ui.explorer

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
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
    onOpenDrawer: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val pagingItems = viewModel.pagedObjects.collectAsLazyPagingItems()
    val snackbarHostState = remember { SnackbarHostState() }

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
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(
                        count = pagingItems.itemCount,
                        key = pagingItems.itemKey { it.objectKey },
                        contentType = pagingItems.itemContentType { it.isDirectory }
                    ) { index ->
                        val item = pagingItems[index]
                        if (item != null) {
                            if (item.isDirectory) {
                                FolderItem(
                                    item = item,
                                    onClick = { viewModel.navigateToFolder(item.objectKey) }
                                )
                            } else {
                                FileItem(
                                    item = item,
                                    onClick = { /* TODO: Open file viewer */ }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
