package com.example.pipmodewithviews.data.model

import com.google.gson.annotations.SerializedName

data class VideosResponseDto(
    @SerializedName("categories")
    val categories:List<CategoryDto>?
)
