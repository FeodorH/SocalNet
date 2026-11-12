package com.example.socalnet.data.di

import android.content.Context
import androidx.room.Room
import com.example.socalnet.data.local.SocialNetDatabase
import com.example.socalnet.data.local.dao.PostDao
import com.example.socalnet.data.local.dao.SubscriptionDao
import com.example.socalnet.data.local.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SocialNetDatabase =
        Room.databaseBuilder(
            context,
            SocialNetDatabase::class.java,
            "socialnet.db"
        )
            .fallbackToDestructiveMigration() // TODO для MVP; в проде — миграции
            .build()

    @Provides fun providePostDao(db: SocialNetDatabase): PostDao = db.postDao()
    @Provides fun provideUserDao(db: SocialNetDatabase): UserDao = db.userDao()
    @Provides fun provideSubscriptionDao(db: SocialNetDatabase): SubscriptionDao = db.subscriptionDao()
}