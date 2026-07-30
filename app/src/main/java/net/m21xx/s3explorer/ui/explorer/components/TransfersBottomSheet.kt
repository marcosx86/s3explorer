package net.m21xx.s3explorer.ui.explorer.components

import android.text.format.Formatter
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import net.m21xx.s3explorer.domain.transfer.TransferState
import net.m21xx.s3explorer.domain.transfer.TransferStatus
import net.m21xx.s3explorer.domain.transfer.TransferType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransfersBottomSheet(
    transfers: List<TransferState>,
    onDismissRequest: () -> Unit,
    onCancelTransfer: (String) -> Unit,
    onClearCompleted: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Active Transfers",
                    style = MaterialTheme.typography.titleMedium
                )
                if (transfers.any { it.status == TransferStatus.COMPLETED || it.status == TransferStatus.CANCELED || it.status == TransferStatus.FAILED }) {
                    TextButton(onClick = onClearCompleted) {
                        Text("Clear Completed")
                    }
                }
            }
            HorizontalDivider()

            if (transfers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No active transfers",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(transfers) { transfer ->
                        TransferItem(transfer = transfer, onCancel = { onCancelTransfer(transfer.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun TransferItem(
    transfer: TransferState,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val progress = if (transfer.totalBytes > 0) transfer.transferredBytes.toFloat() / transfer.totalBytes else 0f
    
    ListItem(
        headlineContent = {
            Text(
                text = transfer.fileName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val statusText = when (transfer.status) {
                        TransferStatus.QUEUED -> "Queued"
                        TransferStatus.IN_PROGRESS -> {
                            val speedStr = Formatter.formatShortFileSize(context, transfer.speedBytesPerSecond) + "/s"
                            val current = Formatter.formatShortFileSize(context, transfer.transferredBytes)
                            val total = Formatter.formatShortFileSize(context, transfer.totalBytes)
                            "$current / $total • $speedStr"
                        }
                        TransferStatus.COMPLETED -> "Completed"
                        TransferStatus.FAILED -> "Failed"
                        TransferStatus.CANCELED -> "Canceled"
                        TransferStatus.PAUSED -> "Paused"
                    }
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (transfer.status == TransferStatus.FAILED) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (transfer.status == TransferStatus.IN_PROGRESS) {
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                if (transfer.status == TransferStatus.IN_PROGRESS || transfer.status == TransferStatus.QUEUED) {
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                if (transfer.status == TransferStatus.FAILED && transfer.errorMessage != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = transfer.errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        leadingContent = {
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                when (transfer.status) {
                    TransferStatus.COMPLETED -> Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50))
                    TransferStatus.FAILED -> Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    else -> Icon(
                        imageVector = if (transfer.type == TransferType.UPLOAD) Icons.Default.Upload else Icons.Default.Download,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        trailingContent = {
            if (transfer.status == TransferStatus.IN_PROGRESS || transfer.status == TransferStatus.QUEUED) {
                IconButton(onClick = onCancel) {
                    Icon(Icons.Default.Cancel, contentDescription = "Cancel")
                }
            }
        }
    )
}
