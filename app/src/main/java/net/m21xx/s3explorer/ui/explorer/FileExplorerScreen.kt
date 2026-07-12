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

    BackHandler(enabled = uiState.currentPrefix.isNotEmpty()) {
        viewModel.navigateUp()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (uiState.currentPrefix.isEmpty()) "Files" else uiState.currentPrefix.trimEnd('/').substringAfterLast('/')
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

            if (pagingItems.loadState.refresh is LoadState.NotLoading && pagingItems.itemCount == 0 && !uiState.isSyncing) {
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
