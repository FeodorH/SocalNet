package com.example.socalnet.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.socalnet.data.local.entity.PostEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {

    @Query("SELECT * FROM posts ORDER BY createdAtEpochMillis DESC LIMIT :limit")
    fun observeGeneralFeed(limit: Int = 50): Flow<List<PostEntity>>

    @Query("""
        SELECT * FROM posts 
        WHERE authorId IN (
            SELECT authorId FROM subscriptions WHERE userId = :userId
        )
        ORDER BY createdAtEpochMillis DESC 
        LIMIT :limit
    """)
    fun observeSubscriptionFeed(userId: String, limit: Int = 50): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE authorId = :authorId ORDER BY createdAtEpochMillis DESC")
    fun observeUserPosts(authorId: String): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE id = :postId")
    fun observePost(postId: String): Flow<PostEntity?>

    @Query("SELECT * FROM posts WHERE id = :postId")
    suspend fun getPost(postId: String): PostEntity?

    @Upsert
    suspend fun upsertPosts(posts: List<PostEntity>)

    @Upsert
    suspend fun upsertPost(post: PostEntity)

    @Query("DELETE FROM posts WHERE id = :postId")
    suspend fun deletePost(postId: String)

    @Query("DELETE FROM posts WHERE cachedAt < :threshold")
    suspend fun clearOldCache(threshold: Long)

    @Query("""
    UPDATE posts 
    SET authorName = :name, authorAvatarUrl = :avatarUrl
    WHERE authorId = :authorId
""")
    suspend fun updateAuthorInPosts(
        authorId: String,
        name: String,
        avatarUrl: String?
    )

    @Query("""
    UPDATE posts 
    SET reactionsTotal = :total, 
        reactionsByType = :byType, 
        myReactionType = :myReaction 
    WHERE id = :postId
""")
    suspend fun updateReactions(
        postId: String,
        total: Int,
        byType: Map<String, Int>,
        myReaction: String?
    )
}