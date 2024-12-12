package com.example.pipmodewithviews.ui.common

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.provider.Settings

class AutoRotateObserver(
    handler: Handler,
    private val context: Context,
    private val autoRotateListener: (Boolean) -> Unit
) : ContentObserver(handler) {

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        super.onChange(selfChange, uri)

        val isAutoRotateEnabled = Settings.System.getInt(context.contentResolver, Settings.System.ACCELEROMETER_ROTATION, 0) == 1
        autoRotateListener(isAutoRotateEnabled)
    }

    fun registerObserver() {
        context.contentResolver.registerContentObserver(
            Settings.System.getUriFor(Settings.System.ACCELEROMETER_ROTATION),
            true,
            this
        )
    }

    fun unregisterObserver() {
        context.contentResolver.unregisterContentObserver(this)
    }
}