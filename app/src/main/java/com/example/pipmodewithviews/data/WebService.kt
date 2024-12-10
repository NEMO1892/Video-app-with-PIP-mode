package com.example.pipmodewithviews.data

import com.example.pipmodewithviews.data.model.VideosResponseDto
import retrofit2.Response
import retrofit2.http.GET

interface WebService {

    @GET(GET_VIDEOS)
    suspend fun getVideos(): Response<VideosResponseDto>

    private companion object {

        const val GET_VIDEOS = "/bikashthapa01/myvideos-android-app/master/data.json"
    }
}