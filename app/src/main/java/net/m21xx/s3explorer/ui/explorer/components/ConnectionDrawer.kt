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
            val timeText = if (stats != null) {
                DateUtils.getRelativeTimeSpanString(stats.lastUpdated, System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS)
            } else {
                "Unknown"
            }
            Text(
                text = "Storage ($timeText)",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refresh storage",
                modifier = Modifier
                    .size(20.dp)
                    .clickable { onRefreshStorageClick() },
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { 0.5f }, // Placeholder visual progress
            modifier = Modifier.fillMaxWidth(),
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        
        if (stats != null) {
            val sizeMb = stats.sizeBytes / (1024 * 1024f)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = String.format("%.2f MB", sizeMb),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "files: ${stats.objectCount}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        } else {
            Text(
                text = "Calculate storage...",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
