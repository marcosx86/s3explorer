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
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
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
import kotlinx.coroutines.launch
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Folder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import net.m21xx.s3explorer.ui.explorer.components.ConnectionDrawerSheet
import net.m21xx.s3explorer.ui.components.WatermarkBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileExplorerScreen(
    viewModel: FileExplorerViewModel = hiltViewModel(),
    onNavigateToConnections: () -> Unit,
    onNavigateToMediaViewer: (profileId: String, bucketName: String, parentPrefix: String, initialObjectKey: String) -> Unit,
    onNavigateToAccountSettings: (profileId: String) -> Unit,
    onNavigateToTransfers: (profileId: String) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToMediaBackup: () -> Unit,
    onNavigateToTrash: (profileId: String) -> Unit,
    onNavigateToAbout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val pagingItems = viewModel.pagedObjects.collectAsLazyPagingItems()
    val snackbarHostState = remember { SnackbarHostState() }
    val gridState = rememberLazyGridState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    var sortMenuExpanded by remember { mutableStateOf(false) }

    var showNewFolderDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }

    val context = LocalContext.current
    val contentResolver = context.contentResolver
    
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val cursor = contentResolver.query(it, null, null, null, null)
            val nameIndex = cursor?.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            val lastModifiedIndex = cursor?.getColumnIndex(android.provider.DocumentsContract.Document.COLUMN_LAST_MODIFIED)
            
            cursor?.moveToFirst()
            val fileName = nameIndex?.let { idx -> cursor.getString(idx) } ?: "upload_${System.currentTimeMillis()}"
            val lastModified = if (lastModifiedIndex != null && lastModifiedIndex >= 0) cursor.getLong(lastModifiedIndex) else null
            cursor?.close()
            
            try {
                contentResolver.openInputStream(it)?.use { inputStream ->
                    val bytes = inputStream.readBytes()
                    viewModel.uploadFile(fileName, bytes, lastModified)
                }
            } catch (e: Exception) {
                // Ignore or show snackbar
            }
        }
    }

    val tempPhotoFile = remember {
        java.io.File(context.cacheDir, "camera_photo.jpg")
    }

    val photoUri = remember {
        try {
            androidx.core.content.FileProvider.getUriForFile(
                context,
                "net.m21xx.s3explorer.fileprovider",
                tempPhotoFile
            )
        } catch (e: Exception) {
            null
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            try {
                val fileBytes = tempPhotoFile.readBytes()
                val lastModified = tempPhotoFile.lastModified()
                viewModel.uploadFile("photo_${System.currentTimeMillis()}.jpg", fileBytes, lastModified)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    BackHandler(enabled = uiState.currentPrefix.isNotEmpty() || uiState.searchActive) {
        if (uiState.searchActive) {
            viewModel.toggleSearch()
        } else {
            viewModel.navigateUp()
        }
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

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ConnectionDrawerSheet(
                drawerState = uiState.drawerState,
                onAccountSettingsClick = { 
                    coroutineScope.launch { drawerState.close() }
                    onNavigateToAccountSettings(uiState.profileId) 
                },
                onTransfersClick = { 
                    coroutineScope.launch { drawerState.close() }
                    onNavigateToTransfers(uiState.profileId) 
                },
                onSyncClick = { 
                    coroutineScope.launch { drawerState.close() }
                    viewModel.forceSync() 
                },
                onSettingsClick = { 
                    coroutineScope.launch { drawerState.close() }
                    onNavigateToSettings() 
                },
                onMediaBackupClick = { 
                    coroutineScope.launch { drawerState.close() }
                    onNavigateToMediaBackup() 
                },
                onTrashClick = { 
                    coroutineScope.launch { drawerState.close() }
                    onNavigateToTrash(uiState.profileId) 
                },
                onAboutClick = { 
                    coroutineScope.launch { drawerState.close() }
                    onNavigateToAbout()
                },
                onRemoveCredentialsClick = { viewModel.toggleRemoveCredentialsDialog(true) },
                onRefreshStorageClick = { viewModel.refreshStorageStats() }
            )
        }
    ) {
        Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column {
                TopAppBar(
                    title = { 
                        if (uiState.searchActive) {
                            OutlinedTextField(
                                value = uiState.searchQuery,
                                onValueChange = { viewModel.updateSearchQuery(it) },
                                placeholder = { Text("Search files...") },
                                modifier = Modifier.fillMaxWidth().padding(end = 16.dp),
                                singleLine = true,
                                trailingIcon = {
                                    IconButton(onClick = { viewModel.toggleSearch() }) {
                                        Icon(Icons.Default.Close, contentDescription = "Close search")
                                    }
                                }
                            )
                        } else {
                            Breadcrumbs(
                                currentPrefix = uiState.currentPrefix,
                                onNavigate = { prefix -> viewModel.navigateToFolder(prefix) }
                            )
                        }
                    },
                    navigationIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (uiState.currentPrefix.isNotEmpty()) {
                                IconButton(onClick = { viewModel.navigateUp() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                }
                            }
                            IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.toggleFloatingActionDrawer(true) }) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                        }
                        IconButton(onClick = onNavigateToConnections) {
                            Icon(Icons.Default.Group, contentDescription = "Connections")
                        }
                    }
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp).padding(bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box {
                        TextButton(onClick = { sortMenuExpanded = true }) {
                            val sortField = when(uiState.sortBy) {
                                SortBy.NAME -> "Name"
                                SortBy.SIZE -> "Size"
                                SortBy.TYPE -> "Type"
                                SortBy.LAST_MODIFIED -> "Date"
                            }
                            Text(sortField)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                if (uiState.sortDirection == SortDirection.ASCENDING) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                contentDescription = "Sort Direction",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = sortMenuExpanded,
                            onDismissRequest = { sortMenuExpanded = false }
                        ) {
                            Text("Sort by", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), style = MaterialTheme.typography.labelSmall)
                            DropdownMenuItem(
                                text = { Text("Name") },
                                onClick = { viewModel.setSortBy(SortBy.NAME); sortMenuExpanded = false },
                                leadingIcon = { if (uiState.sortBy == SortBy.NAME) Icon(Icons.Default.Check, "Selected") else Spacer(Modifier.width(24.dp)) }
                            )
                            DropdownMenuItem(
                                text = { Text("Size") },
                                onClick = { viewModel.setSortBy(SortBy.SIZE); sortMenuExpanded = false },
                                leadingIcon = { if (uiState.sortBy == SortBy.SIZE) Icon(Icons.Default.Check, "Selected") else Spacer(Modifier.width(24.dp)) }
                            )
                            DropdownMenuItem(
                                text = { Text("Type") },
                                onClick = { viewModel.setSortBy(SortBy.TYPE); sortMenuExpanded = false },
                                leadingIcon = { if (uiState.sortBy == SortBy.TYPE) Icon(Icons.Default.Check, "Selected") else Spacer(Modifier.width(24.dp)) }
                            )
                            DropdownMenuItem(
                                text = { Text("Last updated") },
                                onClick = { viewModel.setSortBy(SortBy.LAST_MODIFIED); sortMenuExpanded = false },
                                leadingIcon = { if (uiState.sortBy == SortBy.LAST_MODIFIED) Icon(Icons.Default.Check, "Selected") else Spacer(Modifier.width(24.dp)) }
                            )
                            
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            
                            Text("Direction", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), style = MaterialTheme.typography.labelSmall)
                            DropdownMenuItem(
                                text = { Text("Ascending") },
                                onClick = { viewModel.setSortDirection(SortDirection.ASCENDING); sortMenuExpanded = false },
                                leadingIcon = { if (uiState.sortDirection == SortDirection.ASCENDING) Icon(Icons.Default.Check, "Selected") else Spacer(Modifier.width(24.dp)) }
                            )
                            DropdownMenuItem(
                                text = { Text("Descending") },
                                onClick = { viewModel.setSortDirection(SortDirection.DESCENDING); sortMenuExpanded = false },
                                leadingIcon = { if (uiState.sortDirection == SortDirection.DESCENDING) Icon(Icons.Default.Check, "Selected") else Spacer(Modifier.width(24.dp)) }
                            )
                            
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            
                            DropdownMenuItem(
                                text = { Text("Show hidden") },
                                onClick = { viewModel.toggleShowHidden(); sortMenuExpanded = false },
                                trailingIcon = { 
                                    Switch(
                                        checked = uiState.showHidden,
                                        onCheckedChange = { viewModel.toggleShowHidden(); sortMenuExpanded = false }
                                    ) 
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Folders first") },
                                onClick = { viewModel.toggleFoldersFirst(); sortMenuExpanded = false },
                                trailingIcon = { 
                                    Switch(
                                        checked = uiState.foldersFirst,
                                        onCheckedChange = { viewModel.toggleFoldersFirst(); sortMenuExpanded = false }
                                    ) 
                                }
                            )
                        }
                    }

                    IconButton(onClick = { /* TODO: Toggle cached mode */ }) {
                        Icon(Icons.Default.Storage, contentDescription = "Cached Mode")
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(onClick = { viewModel.toggleSelectionMode() }) {
                        Icon(
                            imageVector = Icons.Default.Checklist,
                            contentDescription = "Selection Mode",
                            tint = if (uiState.selectionModeActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
 
                    IconButton(onClick = { viewModel.toggleSearch() }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = if (uiState.searchActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    val viewModeIcon = when (uiState.viewMode) {
                        ExplorerViewMode.DETAILED_LIST -> Icons.Default.ViewAgenda
                        ExplorerViewMode.COMPACT_LIST -> Icons.AutoMirrored.Filled.List
                        ExplorerViewMode.GALLERY_SMALL -> Icons.Default.ViewCozy
                        ExplorerViewMode.GALLERY_LARGE -> Icons.Default.CalendarViewDay
                    }
                    IconButton(onClick = { viewModel.toggleViewMode() }) {
                        Icon(viewModeIcon, contentDescription = "Toggle View Mode")
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            WatermarkBackground()

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
                                        isCompact = uiState.viewMode == ExplorerViewMode.COMPACT_LIST,
                                        selectionModeActive = uiState.selectionModeActive,
                                        isSelected = uiState.selectedItems.contains(item.objectKey),
                                        onSelect = { viewModel.toggleItemSelection(item.objectKey) },
                                        onClick = { viewModel.navigateToFolder(item.objectKey) }
                                    )
                                    ExplorerViewMode.GALLERY_SMALL,
                                    ExplorerViewMode.GALLERY_LARGE -> GalleryFolderCardItem(
                                        item = item,
                                        selectionModeActive = uiState.selectionModeActive,
                                        isSelected = uiState.selectedItems.contains(item.objectKey),
                                        onSelect = { viewModel.toggleItemSelection(item.objectKey) },
                                        onClick = { viewModel.navigateToFolder(item.objectKey) }
                                    )
                                }
                            } else {
                                when (uiState.viewMode) {
                                    ExplorerViewMode.DETAILED_LIST -> DetailedListItem(
                                        item = item,
                                        showImageThumbnails = uiState.showImageThumbnails,
                                        showVideoThumbnails = uiState.showVideoThumbnails,
                                        getThumbnailUrl = { viewModel.getThumbnailUrl(it) },
                                        getThumbnailUrlSync = { viewModel.getThumbnailUrlSync(it) },
                                        selectionModeActive = uiState.selectionModeActive,
                                        isSelected = uiState.selectedItems.contains(item.objectKey),
                                        onSelect = { viewModel.toggleItemSelection(item.objectKey) },
                                        onClick = { onNavigateToMediaViewer(uiState.profileId, uiState.bucketName, uiState.currentPrefix, item.objectKey) }
                                    )
                                    ExplorerViewMode.COMPACT_LIST -> CompactListItem(
                                        item = item,
                                        showImageThumbnails = uiState.showImageThumbnails,
                                        showVideoThumbnails = uiState.showVideoThumbnails,
                                        getThumbnailUrl = { viewModel.getThumbnailUrl(it) },
                                        getThumbnailUrlSync = { viewModel.getThumbnailUrlSync(it) },
                                        selectionModeActive = uiState.selectionModeActive,
                                        isSelected = uiState.selectedItems.contains(item.objectKey),
                                        onSelect = { viewModel.toggleItemSelection(item.objectKey) },
                                        onClick = { onNavigateToMediaViewer(uiState.profileId, uiState.bucketName, uiState.currentPrefix, item.objectKey) }
                                    )
                                    ExplorerViewMode.GALLERY_SMALL,
                                    ExplorerViewMode.GALLERY_LARGE -> GalleryCardItem(
                                        item = item,
                                        showImageThumbnails = uiState.showImageThumbnails,
                                        showVideoThumbnails = uiState.showVideoThumbnails,
                                        getThumbnailUrl = { viewModel.getThumbnailUrl(it) },
                                        getThumbnailUrlSync = { viewModel.getThumbnailUrlSync(it) },
                                        selectionModeActive = uiState.selectionModeActive,
                                        isSelected = uiState.selectedItems.contains(item.objectKey),
                                        onSelect = { viewModel.toggleItemSelection(item.objectKey) },
                                        onClick = { onNavigateToMediaViewer(uiState.profileId, uiState.bucketName, uiState.currentPrefix, item.objectKey) }
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

    if (uiState.drawerState.showRemoveCredentialsDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.toggleRemoveCredentialsDialog(false) },
            title = { Text("Remove credentials") },
            text = { Text("Are you sure you want to safely remove these S3 connection credentials from the application? This will NOT wipe any data on your bucket, it simply removes access from this app.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.toggleRemoveCredentialsDialog(false)
                        // TODO: Implement PurgeProfileUseCase and navigate away
                    }
                ) {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.toggleRemoveCredentialsDialog(false) }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (uiState.showFloatingActionDrawer) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.toggleFloatingActionDrawer(false) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .navigationBarsPadding()
            ) {
                Text(
                    text = "Actions",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                ListItem(
                    headlineContent = { Text("New folder") },
                    leadingContent = { Icon(Icons.Default.Folder, contentDescription = null) },
                    modifier = Modifier.clickable {
                        viewModel.toggleFloatingActionDrawer(false)
                        showNewFolderDialog = true
                    }
                )
                ListItem(
                    headlineContent = { Text("Upload file") },
                    leadingContent = { Icon(Icons.Default.Upload, contentDescription = null) },
                    modifier = Modifier.clickable {
                        viewModel.toggleFloatingActionDrawer(false)
                        filePickerLauncher.launch("*/*")
                    }
                )
                ListItem(
                    headlineContent = { Text("Take photo") },
                    leadingContent = { Icon(Icons.Default.CameraAlt, contentDescription = null) },
                    modifier = Modifier.clickable {
                        viewModel.toggleFloatingActionDrawer(false)
                        val photoFileExists = try {
                            if (tempPhotoFile.exists()) tempPhotoFile.delete()
                            tempPhotoFile.createNewFile()
                        } catch (e: Exception) {
                            false
                        }
                        if (photoFileExists && photoUri != null) {
                            cameraLauncher.launch(photoUri)
                        }
                    }
                )
            }
        }
    }

    if (showNewFolderDialog) {
        AlertDialog(
            onDismissRequest = { showNewFolderDialog = false; newFolderName = "" },
            title = { Text("New Folder") },
            text = {
                OutlinedTextField(
                    value = newFolderName,
                    onValueChange = { newFolderName = it },
                    label = { Text("Folder Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newFolderName.isNotBlank()) {
                            viewModel.createFolder(newFolderName)
                        }
                        showNewFolderDialog = false
                        newFolderName = ""
                    }
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewFolderDialog = false; newFolderName = "" }) {
                    Text("Cancel")
                }
            }
        )
    }
}
