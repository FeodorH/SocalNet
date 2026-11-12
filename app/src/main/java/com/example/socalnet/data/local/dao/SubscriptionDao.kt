package com.example.socalnet.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.socalnet.data.local.entity.SubscriptionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubscriptionDao {

    @Query("SELECT authorId FROM subscriptions WHERE userId = :userId")
    suspend fun getSubscriptionIds(userId: String): List<String>

    @Query("SELECT authorId FROM subscriptions WHERE userId = :userId")
    fun observeSubscriptionIds(userId: String): Flow<List<String>>

    @Query("SELECT EXISTS(SELECT 1 FROM subscriptions WHERE userId = :userId AND authorId = :authorId)")
    fun observeIsSubscribed(userId: String, authorId: String): Flow<Boolean>

    @Upsert
    suspend fun upsert(subscription: SubscriptionEntity)

    @Query("DELETE FROM subscriptions WHERE userId = :userId AND authorId = :authorId")
    suspend fun delete(userId: String, authorId: String)

    @Query("DELETE FROM subscriptions WHERE userId = :userId")
    suspend fun clearForUser(userId: String)
}