package com.example.socalnet.data.provider

import android.content.Context
import androidx.core.net.toUri
import com.example.socalnet.core.common.AppError
import com.example.socalnet.core.common.AppResult
import com.example.socalnet.core.model.ImageMetadata
import com.example.socalnet.core.utils.AppDispatchers
import com.example.socalnet.domain.provider.ImageMetadataProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ImageMetadataProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dispatchers: AppDispatchers,
) : ImageMetadataProvider {

    override suspend fun read(uri: String): AppResult<ImageMetadata> =
        withContext(dispatchers.io) {
            try {
                val parsed = uri.toUri()
                val resolver = context.contentResolver

                val mime = resolver.getType(parsed)

                // Надёжный размер — через AssetFileDescriptor.
                // Fallback на available() для провайдеров, которые не отдают длину.
                val size = resolver.openAssetFileDescriptor(parsed, "r")?.use { it.length }
                    ?.takeIf { it > 0 }
                    ?: resolver.openInputStream(parsed)?.use { it.available().toLong() }
                    ?: 0L

                AppResult.Success(ImageMetadata(mimeType = mime, sizeBytes = size))
            } catch (e: Exception) {
                AppResult.Failure(AppError.Unknown(e))
            }
        }
}