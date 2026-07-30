package net.m21xx.s3explorer.ui.explorer.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.m21xx.s3explorer.data.local.entity.S3ObjectEntity
import net.m21xx.s3explorer.domain.StorageStatsSummary
import android.text.format.Formatter
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DeleteConfirmationDialog(
    objectsToDelete: List<String>,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val isMultiple = objectsToDelete.size > 1
    val isFolder = objectsToDelete.size == 1 && objectsToDelete.first().endsWith("/")
    
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Warning, contentDescription = "Warning", tint = MaterialTheme.colorScheme.error) },
        title = {
            Text(if (isMultiple) "Delete ${objectsToDelete.size} items?" else if (isFolder) "Delete folder?" else "Delete file?")
        },
        text = {
            if (isFolder) {
                Text("Are you sure you want to delete this folder and ALL its contents? This action cannot be undone.")
            } else if (isMultiple) {
                Text("Are you sure you want to delete the selected items? This action cannot be undone.")
            } else {
                Text("Are you sure you want to delete '${objectsToDelete.first().substringAfterLast("/")}'? This action cannot be undone.")
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun RenameDialog(
    s3Object: S3ObjectEntity,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val isFolder = s3Object.objectKey.endsWith("/")
    val currentName = if (isFolder) {
        s3Object.objectKey.dropLast(1).substringAfterLast("/")
    } else {
        s3Object.objectKey.substringAfterLast("/")
    }
    
    var newName by remember { mutableStateOf(currentName) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename") },
        text = {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("New name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(newName) },
                enabled = newName.isNotBlank() && newName != currentName
            ) {
                Text("Rename")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun PropertiesDialog(
    s3Object: S3ObjectEntity,
    stats: StorageStatsSummary? = null,
    isLoadingStats: Boolean = false,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isFolder = s3Object.isDirectory || s3Object.objectKey.endsWith("/")
    val name = if (isFolder) s3Object.objectKey.dropLast(1).substringAfterLast("/") else s3Object.objectKey.substringAfterLast("/")
    
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault())
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Properties") },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PropertyRow("Name", name)
                PropertyRow("Key", s3Object.objectKey)
                PropertyRow("Type", if (isFolder) "Folder" else "File")
                
                if (isFolder) {
                    if (isLoadingStats) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Size:", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(80.dp))
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                            Text("Calculating...", style = MaterialTheme.typography.bodySmall)
                        }
                    } else if (stats != null) {
                        PropertyRow("Size", Formatter.formatShortFileSize(context, stats.sizeBytes))
                        PropertyRow("Contains", "${stats.objectCount} objects")
                    } else {
                        PropertyRow("Size", "Unknown")
                    }
                } else {
                    PropertyRow("Size", Formatter.formatShortFileSize(context, s3Object.size))
                    if (s3Object.lastModified > 0) {
                        PropertyRow("Last Modified", dateFormatter.format(Date(s3Object.lastModified)))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun PropertyRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(80.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
