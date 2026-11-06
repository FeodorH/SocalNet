package com.example.socalnet.data.mapper

import com.example.socalnet.core.model.ReactionSummary
import com.example.socalnet.core.model.ReactionType

/**
 * Конвертация Map из Firestore в доменную модель ReactionSummary.
 * Безопасно парсит ключи Enum, игнорируя неизвестные значения.
 */
fun Map<String, Long>?.toReactionSummary(total: Long): ReactionSummary {
    if (this == null) return ReactionSummary.EMPTY

    val byType = this.mapNotNull { (key, value) ->
        // Пытаемся найти Enum по имени (например, "LIKE" -> ReactionType.LIKE)
        val type = runCatching { ReactionType.valueOf(key.uppercase()) }.getOrNull()
        if (type != null) type to value.toInt() else null
    }.toMap()

    return ReactionSummary(
        total = total.toInt(),
        byType = byType
    )
}

/**
 * Конвертация доменной реакции в Map для обновления документа поста.
 * Используется при инкременте/декременте счетчиков.
 */
fun ReactionType.toFirestoreIncrementMap(): Map<String, Any> {
    return mapOf(
        "reactionsTotal" to com.google.firebase.firestore.FieldValue.increment(1),
        "reactionsByType.${this.name}" to com.google.firebase.firestore.FieldValue.increment(1)
    )
}

fun ReactionType.toFirestoreDecrementMap(): Map<String, Any> {
    return mapOf(
        "reactionsTotal" to com.google.firebase.firestore.FieldValue.increment(-1),
        "reactionsByType.${this.name}" to com.google.firebase.firestore.FieldValue.increment(-1)
    )
}