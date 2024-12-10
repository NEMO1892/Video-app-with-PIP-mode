package com.example.pipmodewithviews.data.model

import com.example.pipmodewithviews.domain.model.Video
import com.google.gson.annotations.SerializedName

data class VideoDto(
    @SerializedName("description")
    val description: String?,
    @SerializedName("sources")
    val videoUrls: List<String>?,
    @SerializedName("subtitle")
    val subtitle: String?,
    @SerializedName("thumb")
    val photoUrl: String?,
    @SerializedName("title")
    val title: String?
) {

    fun mapDataToDomain() = Video(
        description = description.orEmpty(),
        videoUrls = videoUrls.orEmpty(),
        subtitle = subtitle.orEmpty(),
        photoUrl = photoUrl.orEmpty(),
        title = title.orEmpty()
    )
}
