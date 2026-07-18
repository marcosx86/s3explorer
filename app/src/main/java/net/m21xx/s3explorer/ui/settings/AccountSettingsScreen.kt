package net.m21xx.s3explorer.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: AccountSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val prefs = uiState.preferences

    var showStorageClassDialog by remember { mutableStateOf(false) }
    var tempStorageClass by remember { mutableStateOf("") }

    var showUploadTimeoutDialog by remember { mutableStateOf(false) }
    var tempUploadTimeout by remember { mutableStateOf("") }

    var showDownloadTimeoutDialog by remember { mutableStateOf(false) }
    var tempDownloadTimeout by remember { mutableStateOf("") }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Account settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Section: Privacy
            SectionHeader("Privacy")
            ListItem(
                headlineContent = { Text("E2E encryption") },
                supportingContent = { Text("Encrypt files when uploading, decrypt when downloading.") },
                trailingContent = {
                    Switch(
                        checked = prefs.filenameEncryptionEnabled,
                        onCheckedChange = { viewModel.toggleFilenameEncryption(it) }
                    )
                },
                modifier = Modifier.clickable { 
                    viewModel.toggleFilenameEncryption(!prefs.filenameEncryptionEnabled) 
                }
            )
            HorizontalDivider()

            // Section: Bucket
            SectionHeader("Bucket")
            ListItem(
                headlineContent = { Text("MD5 verification") },
                supportingContent = { Text("Object hash calculation and Content-MD5 header on upload.") },
                trailingContent = {
                    Switch(
                        checked = prefs.calculateMD5Enabled,
                        onCheckedChange = { viewModel.toggleCalculateMD5(it) }
                    )
                },
                modifier = Modifier.clickable { viewModel.toggleCalculateMD5(!prefs.calculateMD5Enabled) }
            )
            ListItem(
                headlineContent = { Text("Storage Class") },
                supportingContent = { Text(prefs.storageClass.ifEmpty { "Default" }) },
                modifier = Modifier.clickable { 
                    tempStorageClass = prefs.storageClass
                    showStorageClassDialog = true 
                }
            )
            HorizontalDivider()

            // Section: Upload
            SectionHeader("Upload")
            ListItem(
                headlineContent = { Text("Skip same file upload") },
                supportingContent = { Text("Skip upload if size and modification time matches (ignores files < 10KB).") },
                trailingContent = {
                    Switch(
                        checked = prefs.skipSameFileUpload,
                        onCheckedChange = { viewModel.toggleSkipSameFileUpload(it) }
                    )
                },
                modifier = Modifier.clickable { viewModel.toggleSkipSameFileUpload(!prefs.skipSameFileUpload) }
            )
            ListItem(
                headlineContent = { Text("Upload transfers") },
                supportingContent = {
                    Column {
                        Text("Concurrent file uploads: ${prefs.uploadConcurrency}")
                        Slider(
                            value = prefs.uploadConcurrency.toFloat(),
                            onValueChange = { viewModel.updateUploadConcurrency(it.toInt()) },
                            valueRange = 1f..10f,
                            steps = 9
                        )
                    }
                }
            )
            ListItem(
                headlineContent = { Text("Multipart start threshold (MB)") },
                supportingContent = {
                    Column {
                        Text("Files over ${prefs.multipartUploadThresholdMB} MB will use multipart upload.")
                        Slider(
                            value = prefs.multipartUploadThresholdMB.toFloat(),
                            onValueChange = { viewModel.updateMultipartThreshold(it.toInt()) },
                            valueRange = 10f..510f,
                            steps = 24
                        )
                    }
                }
            )
            ListItem(
                headlineContent = { Text("Multipart concurrent parts") },
                supportingContent = {
                    Column {
                        Text("Parallel workers: ${prefs.multipartConcurrentParts}")
                        Slider(
                            value = prefs.multipartConcurrentParts.toFloat(),
                            onValueChange = { viewModel.updateMultipartConcurrentParts(it.toInt()) },
                            valueRange = 1f..15f,
                            steps = 14
                        )
                    }
                }
            )
            ListItem(
                headlineContent = { Text("Multipart chunk size (MB)") },
                supportingContent = {
                    Column {
                        Text("Chunk size: ${prefs.multipartChunkSizeMB} MB")
                        Slider(
                            value = prefs.multipartChunkSizeMB.toFloat(),
                            onValueChange = { viewModel.updateMultipartChunkSizeMB(it.toInt()) },
                            valueRange = 5f..100f,
                            steps = 19
                        )
                    }
                }
            )
            HorizontalDivider()

            // Section: Thumbnails
            SectionHeader("Thumbnails")
            ListItem(
                headlineContent = { Text("Generate thumbnails") },
                supportingContent = { Text("Generates and stores them locally.") },
                trailingContent = {
                    Switch(
                        checked = prefs.generateThumbnailsLocally,
                        onCheckedChange = { viewModel.toggleGenerateThumbnailsLocally(it) }
                    )
                },
                modifier = Modifier.clickable { viewModel.toggleGenerateThumbnailsLocally(!prefs.generateThumbnailsLocally) }
            )
            ListItem(
                headlineContent = { Text("Upload thumbnails") },
                supportingContent = { Text("Store generated thumbnails remotely.") },
                trailingContent = {
                    Switch(
                        checked = prefs.uploadThumbnailsRemotely,
                        onCheckedChange = { viewModel.toggleUploadThumbnailsRemotely(it) },
                        enabled = prefs.generateThumbnailsLocally
                    )
                },
                modifier = Modifier.clickable { 
                    if (prefs.generateThumbnailsLocally) {
                        viewModel.toggleUploadThumbnailsRemotely(!prefs.uploadThumbnailsRemotely) 
                    }
                }
            )
            HorizontalDivider()

            // Section: Cleanup
            SectionHeader("Cleanup")
            ListItem(
                headlineContent = { Text("Clear document cache") },
                supportingContent = { Text("Clears the cache of downloaded objects.") },
                modifier = Modifier.clickable { viewModel.clearDocumentCache() }
            )
            ListItem(
                headlineContent = { Text("Clear thumbnails cache") },
                supportingContent = { Text("Clears the cache of generated thumbnails locally.") },
                modifier = Modifier.clickable { viewModel.clearThumbnailCache() }
            )
            ListItem(
                headlineContent = { Text("Delete pending multipart uploads") },
                supportingContent = { Text("Deletes all pending multipart uploads.") },
                modifier = Modifier.clickable { viewModel.deletePendingMultipartUploads() }
            )
            HorizontalDivider()

            // Section: Network
            SectionHeader("Network")
            ListItem(
                headlineContent = { Text("Upload timeout") },
                supportingContent = { Text("${prefs.uploadTimeoutMs} ms") },
                modifier = Modifier.clickable { 
                    tempUploadTimeout = prefs.uploadTimeoutMs.toString()
                    showUploadTimeoutDialog = true 
                }
            )
            ListItem(
                headlineContent = { Text("Download timeout") },
                supportingContent = { Text("${prefs.downloadTimeoutMs} ms") },
                modifier = Modifier.clickable { 
                    tempDownloadTimeout = prefs.downloadTimeoutMs.toString()
                    showDownloadTimeoutDialog = true 
                }
            )
        }
    }

    if (showStorageClassDialog) {
        AlertDialog(
            onDismissRequest = { showStorageClassDialog = false },
            title = { Text("Storage Class") },
            text = {
                OutlinedTextField(
                    value = tempStorageClass,
                    onValueChange = { tempStorageClass = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g. STANDARD") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setStorageClass(tempStorageClass)
                    showStorageClassDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showStorageClassDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showUploadTimeoutDialog) {
        AlertDialog(
            onDismissRequest = { showUploadTimeoutDialog = false },
            title = { Text("Upload Timeout (ms)") },
            text = {
                OutlinedTextField(
                    value = tempUploadTimeout,
                    onValueChange = { tempUploadTimeout = it.filter { char -> char.isDigit() } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    tempUploadTimeout.toLongOrNull()?.let { viewModel.updateUploadTimeoutMs(it) }
                    showUploadTimeoutDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showUploadTimeoutDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showDownloadTimeoutDialog) {
        AlertDialog(
            onDismissRequest = { showDownloadTimeoutDialog = false },
            title = { Text("Download Timeout (ms)") },
            text = {
                OutlinedTextField(
                    value = tempDownloadTimeout,
                    onValueChange = { tempDownloadTimeout = it.filter { char -> char.isDigit() } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    tempDownloadTimeout.toLongOrNull()?.let { viewModel.updateDownloadTimeoutMs(it) }
                    showDownloadTimeoutDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showDownloadTimeoutDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
    )
}
