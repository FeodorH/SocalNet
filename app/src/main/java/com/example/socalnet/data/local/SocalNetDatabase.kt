package com.example.socalnet.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.socalnet.data.local.converter.Converters
import com.example.socalnet.data.local.dao.PostDao
import com.example.socalnet.data.local.dao.SubscriptionDao
import com.example.socalnet.data.local.dao.UserDao
import com.example.socalnet.data.local.entity.PostEntity
import com.example.socalnet.data.local.entity.SubscriptionEntity
import com.example.socalnet.data.local.entity.UserEntity

@Database(
    entities = [
        PostEntity::class,
        UserEntity::class,
        SubscriptionEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SocialNetDatabase : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun userDao(): UserDao
    abstract fun subscriptionDao(): SubscriptionDao
}