package net.m21xx.s3explorer.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: AccountSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

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
            val prefs = uiState.preferences

            // Section: Cryptography & Privacy
            SectionHeader("Cryptography & Privacy")
            
            ListItem(
                headlineContent = { Text("Filename encryption") },
                supportingContent = { Text("Encrypt file and folder names on the bucket.") },
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

            // Section: Network & Transfer Tuning
            SectionHeader("S3 Network & Transfer Tuning")

            ListItem(
                headlineContent = { Text("Start threshold (MB)") },
                supportingContent = {
                    Column {
                        Text("Files larger than ${prefs.multipartUploadThresholdMB} MB will use multipart upload.")
                        Slider(
                            value = prefs.multipartUploadThresholdMB.toFloat(),
                            onValueChange = { viewModel.updateMultipartThreshold(it.toInt()) },
                            valueRange = 1f..100f,
                            steps = 99
                        )
                    }
                }
            )

            ListItem(
                headlineContent = { Text("Upload transfers") },
                supportingContent = {
                    Column {
                        Text("Maximum concurrent uploads: ${prefs.uploadConcurrency}")
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
                headlineContent = { Text("Calculate MD5 hash") },
                supportingContent = { Text("Ensures data integrity during uploads.") },
                trailingContent = {
                    Switch(
                        checked = prefs.calculateMD5Enabled,
                        onCheckedChange = { viewModel.toggleCalculateMD5(it) }
                    )
                },
                modifier = Modifier.clickable {
                    viewModel.toggleCalculateMD5(!prefs.calculateMD5Enabled)
                }
            )

            HorizontalDivider()

            // Section: Cache Lifecycle Management
            SectionHeader("Cache Lifecycle Management")

            ListItem(
                headlineContent = { Text("Clear document cache") },
                supportingContent = { Text("Free up local space used by downloaded documents.") },
                modifier = Modifier.clickable { viewModel.clearDocumentCache() }
            )

            ListItem(
                headlineContent = { Text("Clear thumbnail cache") },
                supportingContent = { Text("Free up local space used by image and video thumbnails.") },
                modifier = Modifier.clickable { viewModel.clearThumbnailCache() }
            )
        }
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
