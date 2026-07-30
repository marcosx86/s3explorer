package net.m21xx.s3explorer.domain.transfer

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton
import java.util.UUID

@Singleton
class TransferManager @Inject constructor() {

    private val _transfers = MutableStateFlow<List<TransferState>>(emptyList())
    val transfers: StateFlow<List<TransferState>> = _transfers.asStateFlow()

    fun addTransfer(transfer: TransferState) {
        _transfers.update { current ->
            current.filter { it.id != transfer.id } + transfer
        }
    }

    fun addDownload(
        profileId: String,
        bucketName: String,
        objectKey: String,
        fileName: String
    ): String {
        val id = UUID.randomUUID().toString()
        addTransfer(
            TransferState(
                id = id, 
                type = TransferType.DOWNLOAD,
                profileId = profileId,
                bucketName = bucketName,
                objectKey = objectKey,
                fileName = fileName,
                totalBytes = 0L,
                transferredBytes = 0L,
                status = TransferStatus.IN_PROGRESS
            )
        )
        return id
    }

    fun updateTransferTotalBytes(id: String, totalBytes: Long) {
        _transfers.update { current ->
            current.map {
                if (it.id == id) it.copy(totalBytes = totalBytes) else it
            }
        }
    }

    fun updateTransferStatus(id: String, status: TransferStatus, errorMessage: String? = null) {
        _transfers.update { current ->
            current.map {
                if (it.id == id) it.copy(status = status, errorMessage = errorMessage) else it
            }
        }
    }

    fun updateTransferProgress(id: String, transferredBytes: Long, speedBytesPerSecond: Long) {
        _transfers.update { current ->
            current.map {
                if (it.id == id) {
                    it.copy(transferredBytes = transferredBytes, speedBytesPerSecond = speedBytesPerSecond)
                } else {
                    it
                }
            }
        }
    }

    fun removeTransfer(id: String) {
        _transfers.update { current ->
            current.filter { it.id != id }
        }
    }

    fun clearCompletedTransfers() {
        _transfers.update { current ->
            current.filter { it.status != TransferStatus.COMPLETED && it.status != TransferStatus.CANCELED }
        }
    }
}
