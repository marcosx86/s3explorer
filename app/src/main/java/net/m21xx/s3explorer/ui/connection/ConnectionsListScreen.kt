package net.m21xx.s3explorer.ui.connection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import net.m21xx.s3explorer.data.local.entity.ConnectionProfileEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionsListScreen(
    viewModel: ConnectionsListViewModel = hiltViewModel(),
    onNavigateToNewConnection: (reuseProfileId: String?) -> Unit,
    onNavigateToExplorer: (profileId: String, bucketName: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Dialog states
    var renameProfileId by remember { mutableStateOf<String?>(null) }
    var newAliasName by remember { mutableStateOf("") }
    
    var globalMenuExpanded by remember { mutableStateOf(false) }
    var showClearListDialog by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.importConnections(it, context) }
    }
    
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        uri?.let { viewModel.exportConnections(it, context) }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Connections") },
                actions = {
                    IconButton(onClick = { onNavigateToNewConnection(null) }) {
                        Icon(Icons.Default.Add, contentDescription = "New Connection")
                    }
                    Box {
                        IconButton(onClick = { globalMenuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More")
                        }
                        DropdownMenu(
                            expanded = globalMenuExpanded,
                            onDismissRequest = { globalMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Import Connections") },
                                onClick = {
                                    globalMenuExpanded = false
                                    importLauncher.launch("*/*")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Export Connections") },
                                onClick = {
                                    globalMenuExpanded = false
                                    exportLauncher.launch("s3explorer_connections.dat")
                                }
                            )
                            Divider()
                            DropdownMenuItem(
                                text = { Text("Clear List", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    globalMenuExpanded = false
                                    showClearListDialog = true
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.profiles.isEmpty()) {
                Text(
                    "No connections found. Add one!",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    itemsIndexed(uiState.profiles) { index, profile ->
                        ConnectionItem(
                            profile = profile,
                            isActive = index == 0,
                            onClick = { 
                                viewModel.markProfileAsActive(profile.profileId)
                                onNavigateToExplorer(profile.profileId, profile.defaultBucket) 
                            },
                             onDelete = { viewModel.deleteProfile(profile.profileId) },
                             onRename = { 
                                 renameProfileId = profile.profileId
                                 val hasCustomAlias = profile.alias.isNotBlank() && profile.alias != profile.endpointUrl
                                 newAliasName = if (hasCustomAlias) profile.alias else profile.defaultBucket
                             },
                             onGenerateConfig = { viewModel.generateConfig(profile.profileId) },
                             onReuse = { onNavigateToNewConnection(profile.profileId) }
                        )
                        Divider()
                    }
                }
            }
        }
        
        // Rename Dialog
        if (renameProfileId != null) {
            AlertDialog(
                onDismissRequest = { renameProfileId = null },
                title = { Text("Rename Connection") },
                text = {
                    OutlinedTextField(
                        value = newAliasName,
                        onValueChange = { newAliasName = it },
                        label = { Text("Alias Name") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(onClick = { 
                        viewModel.renameProfile(renameProfileId!!, newAliasName)
                        renameProfileId = null
                    }) { Text("Save") }
                },
                dismissButton = {
                    TextButton(onClick = { renameProfileId = null }) { Text("Cancel") }
                }
            )
        }
        
        // Clear List Dialog
        if (showClearListDialog) {
            AlertDialog(
                onDismissRequest = { showClearListDialog = false },
                title = { Text("Clear Connections") },
                text = { Text("Are you sure you want to delete all your connection profiles? This action cannot be undone.") },
                confirmButton = {
                    TextButton(onClick = { 
                        viewModel.clearAllProfiles()
                        showClearListDialog = false
                    }) { Text("Clear", color = MaterialTheme.colorScheme.error) }
                },
                dismissButton = {
                    TextButton(onClick = { showClearListDialog = false }) { Text("Cancel") }
                }
            )
        }
        
        // Config Dialog
        if (uiState.generatedConfig != null) {
            AlertDialog(
                onDismissRequest = { viewModel.clearGeneratedConfig() },
                title = { Text("Rclone Config") },
                text = {
                    Text(
                        text = uiState.generatedConfig!!,
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.clearGeneratedConfig() }) { Text("Close") }
                }
            )
        }
    }
}

@Composable
fun ConnectionItem(
    profile: ConnectionProfileEntity,
    isActive: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onRename: () -> Unit,
    onGenerateConfig: () -> Unit,
    onReuse: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            val hasCustomAlias = profile.alias.isNotBlank() && profile.alias != profile.endpointUrl
            val displayName = if (hasCustomAlias) {
                profile.alias
            } else if (profile.defaultBucket.isNotBlank()) {
                profile.defaultBucket
            } else {
                profile.endpointUrl
            }

            val subtitle = if (displayName == profile.endpointUrl) {
                profile.accessKey
            } else {
                "${profile.accessKey} @ ${profile.endpointUrl}"
            }

            Text(
                text = displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                color = if (isActive) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
        }
        
        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Options")
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Set custom name") },
                    onClick = { 
                        showMenu = false
                        onRename() 
                    }
                )
                DropdownMenuItem(
                    text = { Text("Show .ini config") },
                    onClick = { 
                        showMenu = false
                        onGenerateConfig() 
                    }
                )
                DropdownMenuItem(
                    text = { Text("Reuse connection details") },
                    onClick = { 
                        showMenu = false
                        onReuse() 
                    }
                )
            }
        }
    }
}
