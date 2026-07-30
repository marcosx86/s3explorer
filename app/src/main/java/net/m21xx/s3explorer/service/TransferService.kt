package net.m21xx.s3explorer.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import net.m21xx.s3explorer.domain.transfer.TransferManager
import net.m21xx.s3explorer.domain.transfer.TransferStatus
import javax.inject.Inject

@AndroidEntryPoint
class TransferService : Service() {

    @Inject
    lateinit var transferManager: TransferManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val notificationManager by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    companion object {
        private const val CHANNEL_ID = "transfer_service_channel"
        private const val NOTIFICATION_ID = 1001

        fun startService(context: Context) {
            val intent = Intent(context, TransferService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        
        fun stopService(context: Context) {
            val intent = Intent(context, TransferService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification("Starting transfers..."))

        transferManager.transfers.onEach { transfers ->
            val activeTransfers = transfers.filter { it.status == TransferStatus.IN_PROGRESS || it.status == TransferStatus.QUEUED }
            
            if (activeTransfers.isEmpty() && transfers.isNotEmpty()) {
                // If there are no active transfers, we can stop the foreground service
                stopForeground(true)
                stopSelf()
            } else if (activeTransfers.isNotEmpty()) {
                val progress = activeTransfers.sumOf { it.transferredBytes }
                val total = activeTransfers.sumOf { it.totalBytes }
                val msg = "${activeTransfers.size} active transfer(s)"
                val notification = createNotification(msg, progress, total)
                notificationManager.notify(NOTIFICATION_ID, notification)
            }
        }.launchIn(serviceScope)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Transfers",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(contentText: String, progress: Long = 0, total: Long = 0): Notification {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("S3 Transfers")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setOngoing(true)

        if (total > 0) {
            val progressPercent = ((progress.toDouble() / total.toDouble()) * 100).toInt()
            builder.setProgress(100, progressPercent, false)
        } else {
            builder.setProgress(0, 0, true)
        }

        return builder.build()
    }
}
