package com.example.socalnet.data.repository

import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.socalnet.core.common.AppError
import com.example.socalnet.core.common.AppResult
import com.example.socalnet.core.model.ImageRef
import com.example.socalnet.core.utils.AppDispatchers
import com.example.socalnet.data.dto.CloudinaryUploadResult
import com.example.socalnet.domain.repository.MediaRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CloudinaryMediaRepository @Inject constructor(
    private val dispatchers: AppDispatchers
) : MediaRepository {

    override suspend fun upload(local: ImageRef.Local, path: String): AppResult<ImageRef.Remote> =
        withContext(dispatchers.io) {
            try {
                val uri = Uri.parse(local.uri)
                val response = uploadFlow(uri, path).first()
                // Возвращаем и URL, и public_id (обязательно сохраним его в Firestore!)
                AppResult.Success(ImageRef.Remote(response.secureUrl, response.publicId))
            } catch (e: Exception) {
                AppResult.Failure(mapCloudinaryError(e))
            }
        }

    private fun uploadFlow(uri: Uri, folder: String) = callbackFlow {
        MediaManager.get().upload(uri)
            .unsigned("unsigned_preset")
            .option("folder", folder)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {}
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val url = resultData["secure_url"] as? String
                    val publicId = resultData["public_id"] as? String

                    if (url != null && publicId != null) {
                        trySend(CloudinaryUploadResult(url, publicId))
                        close()
                    } else {
                        close(Exception("Cloudinary: missing secure_url or public_id"))
                    }
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    close(Exception("Cloudinary error: ${error.description}"))
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {
                    close(Exception("Cloudinary rescheduled: ${error.description}"))
                }
            })
            .dispatch()

        awaitClose { /* отмена */ }
    }

    override suspend fun delete(url: String): AppResult<Unit> = withContext(dispatchers.io) {
        // Клиент физически не может удалить файл (нет API Secret).
        // Мы просто логируем это и возвращаем Success,
        // чтобы Use Case не падал. Файл удалит GitHub Actions.
        AppResult.Success(Unit)
    }

    override suspend fun deleteAll(urls: List<String>): AppResult<Unit> = withContext(dispatchers.io) {
        AppResult.Success(Unit) // Аналогично
    }

    private fun mapCloudinaryError(e: Exception): AppError {
        return if (e.message?.contains("network", ignoreCase = true) == true) {
            AppError.Network
        } else {
            AppError.Unknown(e)
        }
    }
}