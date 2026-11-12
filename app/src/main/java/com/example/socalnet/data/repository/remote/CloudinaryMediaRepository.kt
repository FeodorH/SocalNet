package com.example.socalnet.data.repository.remote

import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.socalnet.core.common.AppError
import com.example.socalnet.core.common.AppResult
import com.example.socalnet.core.model.ImageRef
import com.example.socalnet.core.model.ImageUploadResult
import com.example.socalnet.core.utils.AppDispatchers
import com.example.socalnet.data.dto.CloudinaryUploadResult
import com.example.socalnet.data.remote.cloudinary.CloudinaryApi
import com.example.socalnet.domain.repository.remote.MediaRepository
import jakarta.inject.Named
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.collections.get

class CloudinaryMediaRepository @Inject constructor(
    private val dispatchers: AppDispatchers,
    private val cloudinaryApi: CloudinaryApi,
    @Named("cloudinaryCloudName") private val cloudName: String,
    @Named("cloudinaryUploadPreset") private val uploadPreset: String,
) : MediaRepository {

    override suspend fun upload(local: ImageRef.Local, path: String): AppResult<ImageUploadResult> =
        withContext(dispatchers.io) {
            try {
                val uri = Uri.parse(local.uri)
                val response = uploadFlow(uri, path).first()
                AppResult.Success(
                    ImageUploadResult(
                        imageRef = ImageRef.Remote(response.secureUrl, response.publicId),
                        deleteToken = response.deleteToken
                    )
                )
            } catch (e: Exception) {
                AppResult.Failure(mapCloudinaryError(e))
            }
        }

    private fun uploadFlow(uri: Uri, folder: String) = callbackFlow {
        MediaManager.get().upload(uri)
            .unsigned(uploadPreset)
            .option("folder", folder)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {}
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val url = resultData["secure_url"] as? String
                    val publicId = resultData["public_id"] as? String
                    val deleteToken = resultData["delete_token"] as? String

                    if (url != null && publicId != null) {
                        trySend(CloudinaryUploadResult(url, publicId, deleteToken))
                        close()
                    } else {
                        close(Exception("Cloudinary: missing required fields"))
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

    override suspend fun deleteByToken(token: String): AppResult<Unit> =
        withContext(dispatchers.io) {
            try {
                val response = cloudinaryApi.deleteByToken(cloudName, token)
                if (response.isSuccessful) AppResult.Success(Unit)
                else AppResult.Failure(
                    AppError.Server(
                        code = response.code().toString(),
                        details = response.errorBody()?.string()
                    )
                )
            } catch (e: Exception) {
                AppResult.Failure(AppError.Unknown(e))
            }
        }

    override suspend fun deleteByTokens(tokens: List<String>): AppResult<Unit> =
        withContext(dispatchers.io) {
            val failures = tokens.mapNotNull { token ->
                when (val r = deleteByToken(token)) {
                    is AppResult.Success -> null
                    is AppResult.Failure -> token to r.error
                }
            }
            if (failures.isEmpty()) {
                AppResult.Success(Unit)
            } else {
                AppResult.Failure(
                    AppError.Server(
                        code = "DELETE_BY_TOKEN_PARTIAL",
                        details = "Не удалось удалить ${failures.size}/${tokens.size}"
                    )
                )
            }
        }

    private fun mapCloudinaryError(e: Exception): AppError {
        return if (e.message?.contains("network", ignoreCase = true) == true) {
            AppError.Network
        } else {
            AppError.Unknown(e)
        }
    }
}