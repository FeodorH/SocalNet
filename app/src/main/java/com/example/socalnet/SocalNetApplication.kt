package com.example.socalnet

import android.app.Application
import com.cloudinary.android.MediaManager
import com.example.socalnet.core.utils.AppDispatchers
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class SocalNetApplication : Application() {

    @Inject
    lateinit var dispatchers: AppDispatchers

    override fun onCreate() {
        super.onCreate()

        val config = mapOf(
            "cloud_name" to BuildConfig.CLOUDINARY_CLOUD_NAME,
            "upload_preset" to BuildConfig.CLOUDINARY_UPLOAD_PRESET,
        )
        MediaManager.init(this, config)
    }
}