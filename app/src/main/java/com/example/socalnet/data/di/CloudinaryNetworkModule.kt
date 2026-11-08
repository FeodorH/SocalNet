package com.example.socalnet.data.di

import com.example.socalnet.data.remote.cloudinary.CloudinaryApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CloudinaryNetworkModule {

    private const val BASE_URL = "https://api.cloudinary.com/"

    @Provides
    @Singleton
    fun provideCloudinaryApi(): CloudinaryApi {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CloudinaryApi::class.java)
    }
}