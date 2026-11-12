package com.example.socalnet.domain.repository.remote

import com.example.socalnet.core.common.AppResult
import com.example.socalnet.core.model.ImageRef
import com.example.socalnet.core.model.ImageUploadResult

interface MediaRepository {
    /** Универсальная загрузка: путь задаёт вызывающий use case. */
    suspend fun upload(local: ImageRef.Local, path: String): AppResult<ImageUploadResult>

    /** методы для компенсации */
    suspend fun deleteByToken(token: String): AppResult<Unit>

    suspend fun deleteByTokens(tokens: List<String>): AppResult<Unit>
}