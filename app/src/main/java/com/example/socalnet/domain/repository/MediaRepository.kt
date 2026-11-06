package com.example.socalnet.domain.repository

import com.example.socalnet.core.common.AppResult
import com.example.socalnet.core.model.ImageRef

interface MediaRepository {
    /** Универсальная загрузка: путь задаёт вызывающий use case. */
    suspend fun upload(local: ImageRef.Local, path: String): AppResult<ImageRef.Remote>

    /** Удаление по URL или storage path. */
    suspend fun delete(url: String): AppResult<Unit>

    /** Удаление по списку — для компенсации при сбое батча. */
    suspend fun deleteAll(urls: List<String>): AppResult<Unit>
}