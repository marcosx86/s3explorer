package net.m21xx.s3explorer.ui.explorer

import android.text.format.Formatter
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CoPresent
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import android.webkit.MimeTypeMap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import net.m21xx.s3explorer.data.local.entity.S3ObjectEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun getPlaceholderIcon(extension: String, mimeType: String?): androidx.compose.ui.graphics.vector.ImageVector {
    if (mimeType != null) {
        when {
            mimeType.startsWith("image/") -> return Icons.Default.Image
            mimeType.startsWith("video/") -> return Icons.Default.VideoFile
            mimeType.startsWith("audio/") -> return Icons.Default.AudioFile
            mimeType.startsWith("text/") -> return Icons.Default.Description
            mimeType == "application/zip" || 
            mimeType == "application/x-tar" || 
            mimeType == "application/x-rar-compressed" -> return Icons.Default.FolderZip
            mimeType == "application/vnd.android.package-archive" -> return Icons.Default.Android
            mimeType == "application/pdf" -> return Icons.Default.PictureAsPdf
            mimeType == "application/json" || 
            mimeType == "application/javascript" || 
            mimeType == "application/xml" -> return Icons.Default.Code
            mimeType == "text/csv" || 
            mimeType == "application/vnd.ms-excel" || 
            mimeType == "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" -> return Icons.Default.TableChart
            mimeType == "application/vnd.ms-powerpoint" || 
            mimeType == "application/vnd.openxmlformats-officedocument.presentationml.presentation" -> return Icons.Default.CoPresent
        }
    }
    
    return when (extension) {
        "jpg", "jpeg", "png", "gif", "webp", "bmp", "tiff", "svg", "heic", "heif" -> Icons.Default.Image
        "mp4", "mkv", "mov", "avi", "flv", "webm", "wmv", "3gp", "mpeg" -> Icons.Default.VideoFile
        "mp3", "wav", "flac", "m4a", "ogg", "aac", "wma", "opus", "mid", "midi" -> Icons.Default.AudioFile
        "zip", "rar", "7z", "tar", "gz", "tgz", "z", "bz2", "bzip2", "xz", "lzh", "lzma", "cab" -> Icons.Default.FolderZip
        "apk" -> Icons.Default.Android
        "deb", "rpm", "exe", "msi", "bat", "cmd", "sh", "bin", "appimage", "dmg" -> Icons.Default.Build
        "pdf" -> Icons.Default.PictureAsPdf
        "json", "xml", "html", "htm", "css", "js", "ts", "kt", "java", "py", "cpp", "c", "h", "cs", "go", "rs", "rb", "php", "yaml", "yml", "sql" -> Icons.Default.Code
        "xls", "xlsx", "csv", "ods", "numbers" -> Icons.Default.TableChart
        "ppt", "pptx", "odp", "key" -> Icons.Default.CoPresent
        "txt", "log", "ini", "conf", "cfg", "md", "rtf", "doc", "docx", "odt", "pages", "epub", "mobi" -> Icons.Default.Description
        "ttf", "otf", "woff", "woff2", "eot" -> Icons.Default.FontDownload
        else -> Icons.AutoMirrored.Filled.InsertDriveFile
    }
}

@Composable
private fun getPlaceholderTint(): Color {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    return if (isDark) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
}

@Composable
fun FolderItem(
    item: S3ObjectEntity,
    isCompact: Boolean = false,
    onClick: () -> Unit
) {
    val folderName = item.objectKey.removePrefix(item.parentPrefix).removeSuffix("/")
    if (isCompact) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = "Folder",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = folderName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium
            )
            IconButton(
                onClick = { /* TODO: Context menu */ },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.MoreVert, contentDescription = "Options", modifier = Modifier.size(20.dp))
            }
        }
    } else {
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
    showImageThumbnails: Boolean,
    showVideoThumbnails: Boolean,
    getThumbnailUrl: suspend (S3ObjectEntity) -> String?,
    getThumbnailUrlSync: (S3ObjectEntity) -> String?,
    onClick: () -> Unit
) {
    val fileName = item.objectKey.removePrefix(item.parentPrefix)
    val context = LocalContext.current
    val formattedSize = Formatter.formatShortFileSize(context, item.size)
    val formattedDate = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", Locale.getDefault()).format(Date(item.lastModified))
    
    val filename = item.objectKey.substringAfterLast('/')
    val extension = if (filename.contains('.')) filename.substringAfterLast('.').lowercase() else ""
    val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    val isImage = mimeType?.startsWith("image/") == true
    val isVideo = mimeType?.startsWith("video/") == true
    val placeholderIcon = getPlaceholderIcon(extension, mimeType)
    
    val shouldShowThumbnail = (isImage && showImageThumbnails) || (isVideo && showVideoThumbnails)

    var url by remember(item.objectKey) { mutableStateOf(if (shouldShowThumbnail) getThumbnailUrlSync(item) else null) }
    
    LaunchedEffect(item.objectKey) {
        if (url == null && shouldShowThumbnail) {
            url = getThumbnailUrl(item)
        }
    }

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
            if (shouldShowThumbnail) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(url)
                        .size(128)
                        .crossfade(true)
                        .bitmapConfig(android.graphics.Bitmap.Config.RGB_565)
                        .apply {
                            if (isVideo) {
                                decoderFactory { result, options, _ ->
                                    VideoFrameDecoder(result.source, options)
                                }
                            }
                        }
                        .build(),
                    contentDescription = "File thumbnail",
                    loading = {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(placeholderIcon, contentDescription = null, tint = getPlaceholderTint(), modifier = Modifier.size(24.dp))
                        }
                    },
                    error = {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(placeholderIcon, contentDescription = null, tint = getPlaceholderTint(), modifier = Modifier.size(24.dp))
                        }
                    },
                    modifier = Modifier.size(48.dp)
                )
            } else {
                Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                    Icon(placeholderIcon, contentDescription = null, tint = getPlaceholderTint(), modifier = Modifier.size(32.dp))
                }
            }
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
    showImageThumbnails: Boolean,
    showVideoThumbnails: Boolean,
    getThumbnailUrl: suspend (S3ObjectEntity) -> String?,
    getThumbnailUrlSync: (S3ObjectEntity) -> String?,
    onClick: () -> Unit
) {
    val fileName = item.objectKey.removePrefix(item.parentPrefix)
    val context = LocalContext.current
    
    val filename = item.objectKey.substringAfterLast('/')
    val extension = if (filename.contains('.')) filename.substringAfterLast('.').lowercase() else ""
    val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    val isImage = mimeType?.startsWith("image/") == true
    val isVideo = mimeType?.startsWith("video/") == true
    val placeholderIcon = getPlaceholderIcon(extension, mimeType)
    
    val shouldShowThumbnail = (isImage && showImageThumbnails) || (isVideo && showVideoThumbnails)

    var url by remember(item.objectKey) { mutableStateOf(if (shouldShowThumbnail) getThumbnailUrlSync(item) else null) }
    
    LaunchedEffect(item.objectKey) {
        if (url == null && shouldShowThumbnail) {
            url = getThumbnailUrl(item)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (shouldShowThumbnail) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context)
                    .data(url)
                    .size(96)
                    .crossfade(true)
                    .bitmapConfig(android.graphics.Bitmap.Config.RGB_565)
                    .apply {
                        if (isVideo) {
                            decoderFactory { result, options, _ ->
                                VideoFrameDecoder(result.source, options)
                            }
                        }
                    }
                    .build(),
                contentDescription = "File thumbnail",
                loading = {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(placeholderIcon, contentDescription = null, tint = getPlaceholderTint(), modifier = Modifier.size(20.dp))
                    }
                },
                error = {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(placeholderIcon, contentDescription = null, tint = getPlaceholderTint(), modifier = Modifier.size(20.dp))
                    }
                },
                modifier = Modifier.size(28.dp)
            )
        } else {
            Box(modifier = Modifier.size(28.dp), contentAlignment = Alignment.Center) {
                Icon(placeholderIcon, contentDescription = null, tint = getPlaceholderTint(), modifier = Modifier.size(20.dp))
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = fileName,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        IconButton(
            onClick = { /* TODO: Context menu */ },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(Icons.Default.MoreVert, contentDescription = "Options", modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun GalleryCardItem(
    item: S3ObjectEntity,
    showImageThumbnails: Boolean,
    showVideoThumbnails: Boolean,
    getThumbnailUrl: suspend (S3ObjectEntity) -> String?,
    getThumbnailUrlSync: (S3ObjectEntity) -> String?,
    onClick: () -> Unit
) {
    val fileName = item.objectKey.removePrefix(item.parentPrefix)
    val context = LocalContext.current
    
    val filename = item.objectKey.substringAfterLast('/')
    val extension = if (filename.contains('.')) filename.substringAfterLast('.').lowercase() else ""
    val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    val isImage = mimeType?.startsWith("image/") == true
    val isVideo = mimeType?.startsWith("video/") == true
    val placeholderIcon = getPlaceholderIcon(extension, mimeType)
    
    val shouldShowThumbnail = (isImage && showImageThumbnails) || (isVideo && showVideoThumbnails)

    var url by remember(item.objectKey) { mutableStateOf(if (shouldShowThumbnail) getThumbnailUrlSync(item) else null) }
    
    LaunchedEffect(item.objectKey) {
        if (url == null && shouldShowThumbnail) {
            url = getThumbnailUrl(item)
        }
    }

    ElevatedCard(
        modifier = Modifier
            .padding(8.dp)
            .aspectRatio(1f)
            .clickable { onClick() }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (shouldShowThumbnail) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(url)
                        .size(720)
                        .crossfade(true)
                        .bitmapConfig(android.graphics.Bitmap.Config.RGB_565)
                        .apply {
                            if (isVideo) {
                                decoderFactory { result, options, _ ->
                                    VideoFrameDecoder(result.source, options)
                                }
                            }
                        }
                        .build(),
                    contentDescription = "File thumbnail",
                    loading = {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(placeholderIcon, contentDescription = null, tint = getPlaceholderTint(), modifier = Modifier.size(48.dp))
                        }
                    },
                    error = {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(placeholderIcon, contentDescription = null, tint = getPlaceholderTint(), modifier = Modifier.size(48.dp))
                        }
                    },
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(placeholderIcon, contentDescription = null, tint = getPlaceholderTint(), modifier = Modifier.size(64.dp))
                }
            }
            
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
