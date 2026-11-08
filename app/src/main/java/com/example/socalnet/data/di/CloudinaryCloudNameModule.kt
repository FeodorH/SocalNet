package com.example.socalnet.data.di

import com.example.socalnet.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Named
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CloudinaryConfigModule {

    @Provides
    @Singleton
    @Named("cloudinaryCloudName")
    fun provideCloudinaryCloudName(): String = BuildConfig.CLOUDINARY_CLOUD_NAME
}