package com.example.socalnet.data.di

import com.example.socalnet.data.repository.CloudinaryMediaRepository
import com.example.socalnet.data.repository.FirebaseAuthRepository
import com.example.socalnet.data.repository.FirebaseFeedRepository
import com.example.socalnet.data.repository.FirebasePostRepository
import com.example.socalnet.data.repository.FirebaseReactionRepository
import com.example.socalnet.data.repository.FirebaseSubscriptionRepository
import com.example.socalnet.data.repository.FirebaseUserRepository
import com.example.socalnet.domain.repository.AuthRepository
import com.example.socalnet.domain.repository.FeedRepository
import com.example.socalnet.domain.repository.MediaRepository
import com.example.socalnet.domain.repository.PostRepository
import com.example.socalnet.domain.repository.ReactionRepository
import com.example.socalnet.domain.repository.SubscriptionRepository
import com.example.socalnet.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindAuthRepository(impl: FirebaseAuthRepository): AuthRepository

    @Binds @Singleton
    abstract fun bindUserRepository(impl: FirebaseUserRepository): UserRepository

    @Binds @Singleton
    abstract fun bindPostRepository(impl: FirebasePostRepository): PostRepository

    @Binds @Singleton
    abstract fun bindFeedRepository(impl: FirebaseFeedRepository): FeedRepository

    @Binds @Singleton
    abstract fun bindSubscriptionRepository(impl: FirebaseSubscriptionRepository): SubscriptionRepository

    @Binds @Singleton
    abstract fun bindReactionRepository(impl: FirebaseReactionRepository): ReactionRepository

    @Binds @Singleton
    abstract fun bindMediaRepository(impl: CloudinaryMediaRepository): MediaRepository
}