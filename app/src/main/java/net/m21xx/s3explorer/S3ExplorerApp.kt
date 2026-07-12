package net.m21xx.s3explorer

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class S3ExplorerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize any application-wide components here
    }
}
