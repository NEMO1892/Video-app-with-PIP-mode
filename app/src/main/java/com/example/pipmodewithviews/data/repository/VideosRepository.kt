package com.example.pipmodewithviews.data.repository

import com.example.pipmodewithviews.data.WebService
import com.example.pipmodewithviews.domain.VideosRepositoryContract
import com.example.pipmodewithviews.domain.model.Video
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.HttpException
import javax.inject.Inject

class VideosRepository @Inject constructor(
    private val webService: WebService
) : VideosRepositoryContract {

    override suspend fun getVideos(): Flow<List<Video>> = flow {
        val response = webService.getVideos()
        if (response.isSuccessful) {
            emit(response.body()?.categories?.first()?.videos?.map {
                it.mapDataToDomain()
            } ?: emptyList())
        } else {
            throw HttpException(response)
        }
    }.flowOn(Dispatchers.IO)
}