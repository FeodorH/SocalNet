package com.example.socalnet.core.utils

import com.example.socalnet.core.model.PageCursor
import com.google.firebase.Timestamp
import java.util.Date

/**
 * Единый кодек для PageCursor во всех репозиториях.
 * Формат: "{epochMillis}|{documentId}" — tie-breaker по id.
 */
object CursorCodec {

    fun encode(timestamp: Timestamp, id: String): PageCursor =
        PageCursor("${timestamp.toDate().time}|$id")

    fun decode(cursor: PageCursor): Pair<Timestamp, String> {
        val parts = cursor.value.split("|")
        return Timestamp(parts[0].toLong(), 0) to parts[1]
    }
}