package com.example.pipmodewithviews.domain

import com.example.pipmodewithviews.domain.model.Video
import kotlinx.coroutines.flow.Flow

interface VideosRepositoryContract {

    suspend fun getVideos(): Flow<List<Video>>
}