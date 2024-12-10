package com.example.pipmodewithviews.ui

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

fun Context.isPIPSupported() =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && packageManager.hasSystemFeature(
        PackageManager.FEATURE_PICTURE_IN_PICTURE
    )
