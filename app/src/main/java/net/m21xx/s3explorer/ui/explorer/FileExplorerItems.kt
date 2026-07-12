package net.m21xx.s3explorer.ui.explorer

import android.text.format.Formatter
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
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
fun GalleryFolderCardItem(
    item: S3ObjectEntity,
    onClick: () -> Unit
) {
    val folderName = item.objectKey.removePrefix(item.parentPrefix).removeSuffix("/")

    ElevatedCard(
        modifier = Modifier
            .padding(8.dp)
            .aspectRatio(1f)
            .clickable { onClick() }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = "Folder",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(72.dp)
            )
            
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = folderName,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { /* TODO: Context menu */ },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun DetailedListItem(
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
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(null) // TODO: Real image loading via presigned URLs or custom Fetcher
                    .size(128)
                    .crossfade(true)
                    .build(),
                contentDescription = "File thumbnail",
                placeholder = rememberVectorPainter(Icons.Default.InsertDriveFile),
                error = rememberVectorPainter(Icons.Default.InsertDriveFile),
                modifier = Modifier.size(48.dp)
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
fun CompactListItem(
    item: S3ObjectEntity,
    onClick: () -> Unit
) {
    val fileName = item.objectKey.removePrefix(item.parentPrefix)
    val context = LocalContext.current
    
    ListItem(
        modifier = Modifier.clickable { onClick() },
        headlineContent = {
            Text(
                text = fileName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        leadingContent = {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(null)
                    .size(128)
                    .crossfade(true)
                    .build(),
                contentDescription = "File thumbnail",
                placeholder = rememberVectorPainter(Icons.Default.InsertDriveFile),
                error = rememberVectorPainter(Icons.Default.InsertDriveFile),
                modifier = Modifier.size(36.dp)
            )
        },
        trailingContent = {
            IconButton(onClick = { /* TODO: Context menu */ }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Options")
            }
        }
    )
    HorizontalDivider(modifier = Modifier.padding(start = 60.dp))
}

@Composable
fun GalleryCardItem(
    item: S3ObjectEntity,
    onClick: () -> Unit
) {
    val fileName = item.objectKey.removePrefix(item.parentPrefix)
    val context = LocalContext.current

    ElevatedCard(
        modifier = Modifier
            .padding(8.dp)
            .aspectRatio(1f)
            .clickable { onClick() }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(null)
                    .size(1080)
                    .crossfade(true)
                    .build(),
                contentDescription = "File thumbnail",
                placeholder = rememberVectorPainter(Icons.Default.Image),
                error = rememberVectorPainter(Icons.Default.Image),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            
            // Filename overlay
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = fileName,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { /* TODO: Context menu */ },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
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

@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Error Loading Directory",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Composable
fun Breadcrumbs(
    currentPrefix: String,
    onNavigate: (String) -> Unit
) {
    val scrollState = rememberScrollState()
    val parts = currentPrefix.split("/").filter { it.isNotEmpty() }
    
    LaunchedEffect(currentPrefix) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(
            onClick = { onNavigate("") },
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            Text("Files", style = MaterialTheme.typography.titleMedium)
        }
        
        var cumulativePrefix = ""
        parts.forEach { part ->
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            cumulativePrefix += "$part/"
            val targetPrefix = cumulativePrefix
            TextButton(
                onClick = { onNavigate(targetPrefix) },
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                Text(part, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
