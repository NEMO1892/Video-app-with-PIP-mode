package com.example.pipmodewithviews.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Video(
    val description: String,
    val videoUrls: List<String>,
    val subtitle: String,
    val photoUrl: String,
    val title: String
): Parcelable