package net.m21xx.s3explorer.domain.transfer

enum class TransferType {
    UPLOAD,
    DOWNLOAD
}

enum class TransferStatus {
    IN_PROGRESS,
    PAUSED,
    COMPLETED,
    FAILED,
    CANCELED,
    QUEUED
}

data class TransferState(
    val id: String, // Unique UUID for the transfer
    val type: TransferType,
    val profileId: String,
    val bucketName: String,
    val objectKey: String,
    val fileName: String,
    val totalBytes: Long,
    val transferredBytes: Long,
    val status: TransferStatus,
    val speedBytesPerSecond: Long = 0L,
    val errorMessage: String? = null
)
