package com.example.pipmodewithviews.domain.usecase

import com.example.pipmodewithviews.domain.VideosRepositoryContract
import com.example.pipmodewithviews.domain.model.Video
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetVideosUseCase @Inject constructor(private val videosRepository: VideosRepositoryContract) {

    suspend operator fun invoke(): Flow<List<Video>> = videosRepository.getVideos()
}