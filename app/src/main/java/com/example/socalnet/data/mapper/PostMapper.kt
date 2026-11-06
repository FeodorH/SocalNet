package com.example.socalnet.data.mapper

import com.example.socalnet.core.model.ImageRef
import com.example.socalnet.core.model.Post
import com.example.socalnet.core.model.PostAuthor
import com.example.socalnet.core.model.PostDraft
import com.example.socalnet.core.model.ReactionType
import com.example.socalnet.data.dto.PostResponse

/**
 * Маппинг из ответа Firestore в доменную модель.
 *
 * @param author Данные автора (имя, аватар), полученные из кэша или Firestore.
 * @param myReaction Реакция текущего пользователя на этот пост (если есть).
 */
fun PostResponse.toDomain(
    author: PostAuthor,
    myReaction: ReactionType? = null
): Post {
    return Post(
        id = id,
        author = author,
        text = text,
        images = imageUrls.mapIndexed { index, url ->
            val publicId = imagePublicIds.getOrNull(index) // Безопасно берем ID по индексу
            ImageRef.Remote(url, publicId)
        },
        createdAt = createdAt.toDomainInstant(),
        updatedAt = updatedAt?.toDomainInstant(),
        reactions = reactionsByType.toReactionSummary(reactionsTotal),
        myReaction = myReaction
    )
}

/**
 * Маппинг доменного черновика в Map для создания поста.
 * createdAt и updatedAt добавятся в репозитории через FieldValue.serverTimestamp().
 */
fun PostDraft.toCreateMap(
    authorId: String,
    uploadedImages: List<ImageRef.Remote>
): Map<String, Any?> {
    return mapOf(
        "authorId" to authorId,
        "text" to text,
        "imageUrls" to uploadedImages.map { it.url },
        "imagePublicIds" to uploadedImages.map { it.publicId },
        "reactionsTotal" to 0L,
        "reactionsByType" to emptyMap<String, Long>()
    )
}

/**
 * Маппинг для обновления поста.
 */
fun PostDraft.toUpdateMap(uploadedImageUrls: List<String>): Map<String, Any?> {
    return mapOf(
        "text" to text,
        "imageUrls" to uploadedImageUrls
        // updatedAt добавится в репозитории
    )
}