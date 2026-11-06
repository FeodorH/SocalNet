package com.example.socalnet.data.mapper

import com.google.firebase.Timestamp
import java.util.Date
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Конвертация из Firebase Timestamp в доменный kotlin.time.Instant.
 * Если пришел null (например, сервер еще не обработал serverTimestamp),
 * берем текущее время (fallback).
 */
fun Timestamp?.toDomainInstant(): Instant {
    return this?.let { Instant.fromEpochMilliseconds(it.toDate().time) }
        ?: Clock.System.now()
}

/**
 * Конвертация из доменного Instant в Firebase Timestamp.
 * Используется в основном для тестов или если мы вручную задаем время.
 */
fun Instant.toFirebaseTimestamp(): Timestamp {
    return Timestamp(Date(this.toEpochMilliseconds()))
}