package com.example.socalnet.data.di

import com.example.socalnet.data.repository.common.OfflineFirstAuthRepositoryImpl
import com.example.socalnet.data.repository.common.OfflineFirstFeedRepositoryImpl
import com.example.socalnet.data.repository.common.OfflineFirstPostRepositoryImpl
import com.example.socalnet.data.repository.common.OfflineFirstReactionRepositoryImpl
import com.example.socalnet.data.repository.common.OfflineFirstSubscriptionRepositoryImpl
import com.example.socalnet.data.repository.remote.CloudinaryMediaRepository
import com.example.socalnet.data.repository.remote.FirebaseAuthRepository
import com.example.socalnet.data.repository.remote.FirebaseFeedRepository
import com.example.socalnet.data.repository.remote.FirebasePostRepository
import com.example.socalnet.data.repository.remote.FirebaseReactionRepository
import com.example.socalnet.data.repository.remote.FirebaseSubscriptionRepository
import com.example.socalnet.data.repository.remote.FirebaseUserRepository
import com.example.socalnet.data.repository.common.OfflineFirstUserRepositoryImpl
import com.example.socalnet.domain.repository.remote.AuthRepository
import com.example.socalnet.domain.repository.remote.FeedRepository
import com.example.socalnet.domain.repository.remote.MediaRepository
import com.example.socalnet.domain.repository.remote.PostRepository
import com.example.socalnet.domain.repository.remote.ReactionRepository
import com.example.socalnet.domain.repository.remote.SubscriptionRepository
import com.example.socalnet.domain.repository.remote.UserRepository
import com.example.socalnet.domain.repository.common.OfflineFirstAuthRepository
import com.example.socalnet.domain.repository.common.OfflineFirstFeedRepository
import com.example.socalnet.domain.repository.common.OfflineFirstPostRepository
import com.example.socalnet.domain.repository.common.OfflineFirstReactionRepository
import com.example.socalnet.domain.repository.common.OfflineFirstSubscriptionRepository
import com.example.socalnet.domain.repository.common.OfflineFirstUserRepository
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

    @Binds @Singleton
    abstract fun bindOfflineFirstAuthRepository(
        impl: OfflineFirstAuthRepositoryImpl
    ): OfflineFirstAuthRepository

    @Binds @Singleton
    abstract fun bindOfflineFirstUserRepository(
        impl: OfflineFirstUserRepositoryImpl
    ): OfflineFirstUserRepository

    @Binds @Singleton
    abstract fun bindOfflineFirstPostRepository(
        impl: OfflineFirstPostRepositoryImpl
    ): OfflineFirstPostRepository

    @Binds @Singleton
    abstract fun bindOfflineFirstFeedRepository(
        impl: OfflineFirstFeedRepositoryImpl
    ): OfflineFirstFeedRepository

    @Binds @Singleton
    abstract fun bindOfflineFirstSubscriptionRepository(
        impl: OfflineFirstSubscriptionRepositoryImpl
    ): OfflineFirstSubscriptionRepository

    @Binds @Singleton
    abstract fun bindOfflineFirstReactionRepository(
        impl: OfflineFirstReactionRepositoryImpl
    ): OfflineFirstReactionRepository
}