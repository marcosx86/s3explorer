package net.m21xx.s3explorer.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: GlobalSettingsViewModel = hiltViewModel()
) {
    val prefs by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Global Settings") },
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
            // Section: System Integration
            SectionHeader("System Integration & OS Mounting")
            
            ListItem(
                headlineContent = { Text("Enable Mount (SAF)") },
                supportingContent = { Text("Allow Android system to browse this bucket.") },
                trailingContent = {
                    Switch(
                        checked = prefs.enableSafMount,
                        onCheckedChange = { viewModel.toggleSafMount(it) }
                    )
                },
                modifier = Modifier.clickable { 
                    viewModel.toggleSafMount(!prefs.enableSafMount) 
                }
            )

            ListItem(
                headlineContent = { Text("Trust insecure SSL/TLS certificates") },
                supportingContent = { Text("Allows connections to self-signed endpoints like MinIO.") },
                trailingContent = {
                    Switch(
                        checked = prefs.trustSslCertificate,
                        onCheckedChange = { viewModel.toggleTrustSsl(it) }
                    )
                },
                modifier = Modifier.clickable { 
                    viewModel.toggleTrustSsl(!prefs.trustSslCertificate) 
                }
            )

            HorizontalDivider()

            // Section: App Security
            SectionHeader("App Security")

            ListItem(
                headlineContent = { Text("Enable native lock screen") },
                supportingContent = { Text("Require biometrics or PIN to open the app.") },
                trailingContent = {
                    Switch(
                        checked = prefs.enableLockScreen,
                        onCheckedChange = { viewModel.toggleLockScreen(it) }
                    )
                },
                modifier = Modifier.clickable { 
                    viewModel.toggleLockScreen(!prefs.enableLockScreen) 
                }
            )

            HorizontalDivider()

            // Section: View Preferences
            SectionHeader("View Preferences")

            ListItem(
                headlineContent = { Text("Display long date format") },
                trailingContent = {
                    Switch(
                        checked = prefs.displayLongDateFormat,
                        onCheckedChange = { viewModel.toggleLongDateFormat(it) }
                    )
                },
                modifier = Modifier.clickable { 
                    viewModel.toggleLongDateFormat(!prefs.displayLongDateFormat) 
                }
            )

            ListItem(
                headlineContent = { Text("Hide dotfiles") },
                trailingContent = {
                    Switch(
                        checked = prefs.hideDotfiles,
                        onCheckedChange = { viewModel.toggleHideDotfiles(it) }
                    )
                },
                modifier = Modifier.clickable { 
                    viewModel.toggleHideDotfiles(!prefs.hideDotfiles) 
                }
            )

            ListItem(
                headlineContent = { Text("Show image thumbnails") },
                trailingContent = {
                    Switch(
                        checked = prefs.showImageThumbnails,
                        onCheckedChange = { viewModel.toggleShowThumbnails(it) }
                    )
                },
                modifier = Modifier.clickable { 
                    viewModel.toggleShowThumbnails(!prefs.showImageThumbnails) 
                }
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
