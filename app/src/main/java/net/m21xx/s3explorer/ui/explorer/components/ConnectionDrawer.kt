package net.m21xx.s3explorer.ui.explorer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import net.m21xx.s3explorer.ui.explorer.DrawerUIState
import android.text.format.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionDrawerSheet(
    drawerState: DrawerUIState,
    onAccountSettingsClick: () -> Unit,
    onTransfersClick: () -> Unit,
    onSyncClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onMediaBackupClick: () -> Unit,
    onTrashClick: () -> Unit,
    onAboutClick: () -> Unit,
    onRemoveCredentialsClick: () -> Unit,
    onRefreshStorageClick: () -> Unit
) {
    ModalDrawerSheet {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            DrawerHeader(drawerState)

            HorizontalDivider()

            // Menu Group 1 (Account Operations)
            NavigationDrawerItem(
                label = { Text("Account settings") },
                icon = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
                selected = false,
                onClick = onAccountSettingsClick,
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
            NavigationDrawerItem(
                label = { Text("Transfers") },
                icon = { Icon(Icons.Default.SwapVert, contentDescription = null) },
                selected = false,
                onClick = onTransfersClick,
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Menu Group 2 (Global & Utility)
            NavigationDrawerItem(
                label = { Text("Sync") },
                icon = { Icon(Icons.Default.Sync, contentDescription = null) },
                selected = false,
                onClick = onSyncClick,
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
            NavigationDrawerItem(
                label = { Text("Settings") },
                icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                selected = false,
                onClick = onSettingsClick,
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
            NavigationDrawerItem(
                label = { Text("Media backup") },
                icon = { Icon(Icons.Default.CloudUpload, contentDescription = null) },
                selected = false,
                onClick = onMediaBackupClick,
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
            NavigationDrawerItem(
                label = { Text("Trash") },
                icon = { Icon(Icons.Default.Delete, contentDescription = null) },
                selected = false,
                onClick = onTrashClick,
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
            NavigationDrawerItem(
                label = { Text("About") },
                icon = { Icon(Icons.Default.Info, contentDescription = null) },
                selected = false,
                onClick = onAboutClick,
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Destructive Action
            NavigationDrawerItem(
                label = { Text("Remove credentials", color = MaterialTheme.colorScheme.error) },
                icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                selected = false,
                onClick = onRemoveCredentialsClick,
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )

            HorizontalDivider()

            // Footer (Storage Stats)
            DrawerFooter(drawerState, onRefreshStorageClick)
        }
    }
}

@Composable
private fun DrawerHeader(drawerState: DrawerUIState) {
    val profile = drawerState.activeProfile
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "S3",
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.titleMedium
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = profile?.alias ?: "Loading...",
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = profile?.endpointUrl ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DrawerFooter(drawerState: DrawerUIState, onRefreshStorageClick: () -> Unit) {
    val stats = drawerState.storageStats
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Side: Storage Icon + "Storage"
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Storage,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Storage",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Right Side: "X minutes ago" + Refresh/Loading
            Row(verticalAlignment = Alignment.CenterVertically) {
                val timeText = if (stats != null) {
                    DateUtils.getRelativeTimeSpanString(stats.lastUpdated, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS)
                } else {
                    "Unknown"
                }
                Text(
                    text = timeText.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                
                if (drawerState.isCalculatingStorageStats) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh storage",
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { onRefreshStorageClick() },
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Progress bar (S3 doesn't have a max size by default, so we show a small arbitrary percentage or indeterminate if loading)
        if (drawerState.isCalculatingStorageStats) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        } else {
            // Static arbitrary progress for now, could be scaled against a 1TB quota
            val progressVal = if (stats != null) (stats.sizeBytes.toFloat() / (1024f * 1024f * 1024f * 100f)).coerceIn(0f, 1f) else 0f
            LinearProgressIndicator(
                progress = { progressVal.coerceAtLeast(0.01f) },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        if (stats != null) {
            val sizeMb = stats.sizeBytes / (1024 * 1024f)
            Text(
                text = String.format("%.2f MB used, files: %d", sizeMb, stats.objectCount),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Text(
                text = "Calculate storage...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
