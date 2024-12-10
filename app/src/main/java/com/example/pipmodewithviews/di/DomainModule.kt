package com.example.pipmodewithviews.di

import com.example.pipmodewithviews.data.repository.VideosRepository
import com.example.pipmodewithviews.domain.VideosRepositoryContract
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
interface DomainModule {

    @Binds
    fun provideVideosRepository(repository: VideosRepository): VideosRepositoryContract
}