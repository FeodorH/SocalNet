package com.example.socalnet.presentation.common

import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.time.Instant

fun formatRelativeTime(instant: Instant): String {
    val now = kotlin.time.Clock.System.now()
    val diffSeconds = (now - instant).inWholeSeconds

    return when {
        diffSeconds < 60 -> "только что"
        diffSeconds < 3_600 -> "${diffSeconds / 60} мин назад"
        diffSeconds < 86_400 -> "${diffSeconds / 3_600} ч назад"
        diffSeconds < 172_800 -> "вчера"
        diffSeconds < 604_800 -> "${diffSeconds / 86_400} дн назад"
        else -> {
            val date = instant.toJavaInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            date.format(DateTimeFormatter.ofPattern("d MMM"))
        }
    }
}

private fun Instant.toJavaInstant(): java.time.Instant =
    java.time.Instant.ofEpochMilli(toEpochMilliseconds())