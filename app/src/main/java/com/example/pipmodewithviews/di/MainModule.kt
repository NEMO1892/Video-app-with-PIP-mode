package com.example.pipmodewithviews.di

import android.content.Context
import com.example.pipmodewithviews.data.WebService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class MainModule {

    @Provides
    fun provideContext(@ApplicationContext it: Context): Context = it

    @Provides
    @Singleton
    fun provideOkHttp(context: Context): OkHttpClient {
        val cacheFile = File(context.cacheDir, "http-cache-videos")
        val cache = Cache(cacheFile, CACHE_SIZE)
        return OkHttpClient
            .Builder()
            .connectTimeout(10000L, TimeUnit.SECONDS)
            .readTimeout(10000L, TimeUnit.SECONDS)
            .writeTimeout(10000L, TimeUnit.SECONDS)
            .cache(cache)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    @Provides
    @Singleton
    fun provideWebService(retrofit: Retrofit): WebService = retrofit.create(WebService::class.java)

    private companion object {

        const val BASE_URL = "https://raw.githubusercontent.com/"
        const val CACHE_SIZE = (10 * 1024 * 1024).toLong()
    }
}
