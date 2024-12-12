package com.example.pipmodewithviews.ui

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.bumptech.glide.Glide
import java.io.Serializable

fun Context.isPIPSupported() =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && packageManager.hasSystemFeature(
        PackageManager.FEATURE_PICTURE_IN_PICTURE
    )

fun ImageView.loadUrlImage(url: String) {
    Glide.with(context)
        .load(url)
        .into(this)
}

inline fun <reified T : Parcelable> Bundle.getParcelableClass(key: String): T? = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getParcelable( key, T::class.java)
    else -> @Suppress("DEPRECATION") getSerializable(key) as? T
}