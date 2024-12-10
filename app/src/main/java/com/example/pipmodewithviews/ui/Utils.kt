package com.example.pipmodewithviews.ui

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.ImageView
import com.bumptech.glide.Glide

fun Context.isPIPSupported() =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && packageManager.hasSystemFeature(
        PackageManager.FEATURE_PICTURE_IN_PICTURE
    )

fun ImageView.loadUrlImage(url: String) {
    Glide.with(context)
        .load(url)
        .into(this)
}
