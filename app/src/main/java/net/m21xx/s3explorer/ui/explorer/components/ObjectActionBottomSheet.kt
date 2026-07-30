package net.m21xx.s3explorer.ui.explorer.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import net.m21xx.s3explorer.data.local.entity.S3ObjectEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObjectActionBottomSheet(
    s3Object: S3ObjectEntity,
    onDismissRequest: () -> Unit,
    onOpenWithClick: () -> Unit,
    onShareClick: () -> Unit,
    onRenameClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onPropertiesClick: () -> Unit,
    onFolderStatsClick: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp) // extra padding for bottom nav/gestures
        ) {
            // Header
            Text(
                text = s3Object.objectKey.substringAfterLast('/').takeIf { it.isNotEmpty() } ?: s3Object.objectKey.dropLast(1).substringAfterLast('/'),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            HorizontalDivider()

            if (s3Object.isDirectory) {
                // Folder Actions
                ListItem(
                    headlineContent = { Text("Rename") },
                    leadingContent = { Icon(Icons.Default.Edit, contentDescription = null) },
                    modifier = Modifier.clickable { onRenameClick() }
                )
                ListItem(
                    headlineContent = { Text("Download (ZIP)") },
                    leadingContent = { Icon(Icons.Default.Download, contentDescription = null) },
                    modifier = Modifier.clickable { onDownloadClick() }
                )
                ListItem(
                    headlineContent = { Text("Folder statistics") },
                    leadingContent = { Icon(Icons.Default.Analytics, contentDescription = null) },
                    modifier = Modifier.clickable { onFolderStatsClick() }
                )
                ListItem(
                    headlineContent = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                    leadingContent = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                    modifier = Modifier.clickable { onDeleteClick() }
                )
            } else {
                // File Actions
                ListItem(
                    headlineContent = { Text("Open with") },
                    leadingContent = { Icon(Icons.Default.OpenInNew, contentDescription = null) },
                    modifier = Modifier.clickable { onOpenWithClick() }
                )
                ListItem(
                    headlineContent = { Text("Share") },
                    leadingContent = { Icon(Icons.Default.Share, contentDescription = null) },
                    modifier = Modifier.clickable { onShareClick() }
                )
                if (!s3Object.objectKey.endsWith("/")) {
                    ListItem(
                        headlineContent = { Text("Rename") },
                        leadingContent = { Icon(Icons.Default.Edit, contentDescription = null) },
                        modifier = Modifier.clickable { onRenameClick() }
                    )
                }
                ListItem(
                    headlineContent = { Text("Download") },
                    leadingContent = { Icon(Icons.Default.Download, contentDescription = null) },
                    modifier = Modifier.clickable { onDownloadClick() }
                )
                ListItem(
                    headlineContent = { Text("Properties") },
                    leadingContent = { Icon(Icons.Default.Info, contentDescription = null) },
                    modifier = Modifier.clickable { onPropertiesClick() }
                )
                ListItem(
                    headlineContent = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                    leadingContent = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                    modifier = Modifier.clickable { onDeleteClick() }
                )
            }
        }
    }
}
