package net.m21xx.s3explorer

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.VideoFrameDecoder
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.Dispatchers
import okhttp3.Dispatcher
import okhttp3.OkHttpClient

@HiltAndroidApp
class S3ExplorerApp : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        // Initialize any application-wide components here
    }

    override fun newImageLoader(): ImageLoader {
        val okHttpClient = OkHttpClient.Builder()
            .dispatcher(Dispatcher().apply {
                maxRequests = 5
                maxRequestsPerHost = 5
            })
            .build()

        return ImageLoader.Builder(this)
            .components {
                add(VideoFrameDecoder.Factory())
            }
            .okHttpClient(okHttpClient)
            .build()
    }
}
