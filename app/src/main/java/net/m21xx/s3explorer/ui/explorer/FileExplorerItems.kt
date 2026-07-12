package net.m21xx.s3explorer.ui.explorer

import android.text.format.Formatter
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import net.m21xx.s3explorer.data.local.entity.S3ObjectEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FolderItem(
    item: S3ObjectEntity,
    onClick: () -> Unit
) {
    val folderName = item.objectKey.removePrefix(item.parentPrefix).removeSuffix("/")
    ListItem(
        modifier = Modifier.clickable { onClick() },
        headlineContent = {
            Text(
                text = folderName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingContent = {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = "Folder",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
        },
        trailingContent = {
            IconButton(onClick = { /* TODO: Context menu */ }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Options")
            }
        }
    )
    HorizontalDivider(modifier = Modifier.padding(start = 72.dp))
}

@Composable
fun FileItem(
    item: S3ObjectEntity,
    onClick: () -> Unit
) {
    val fileName = item.objectKey.removePrefix(item.parentPrefix)
    val context = LocalContext.current
    val formattedSize = Formatter.formatShortFileSize(context, item.size)
    val formattedDate = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", Locale.getDefault()).format(Date(item.lastModified))
    
    ListItem(
        modifier = Modifier.clickable { onClick() },
        headlineContent = {
            Text(
                text = fileName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            Text(text = "$formattedDate • $formattedSize")
        },
        leadingContent = {
            Icon(
                imageVector = Icons.Default.InsertDriveFile,
                contentDescription = "File",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(40.dp)
            )
        },
        trailingContent = {
            IconButton(onClick = { /* TODO: Context menu */ }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Options")
            }
        }
    )
    HorizontalDivider(modifier = Modifier.padding(start = 72.dp))
}

@Composable
fun EmptyDirectoryState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Folder,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Tap + to add files here",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
