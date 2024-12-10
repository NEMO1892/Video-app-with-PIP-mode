package com.example.pipmodewithviews.domain.model

data class Video(
    val description: String,
    val videoUrls: List<String>,
    val subtitle: String,
    val photoUrl: String,
    val title: String
)