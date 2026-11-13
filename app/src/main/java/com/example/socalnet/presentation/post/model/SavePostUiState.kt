package com.example.socalnet.presentation.post.model

import com.example.socalnet.core.model.ImageRef

/**
 * Состояние формы создания/редактирования поста.
 *
 * UiState здесь не нужен: это форма, а не загрузка данных.
 * Прогресс публикации — обычный флаг.
 */
data class SavePostUiState(
    val text: String = "",
    val newImageUris: List<String> = emptyList(),
    val existingImages: List<ImageRef.Remote> = emptyList(),
    val existingPostId: String? = null,       // null = create, non-null = edit
    val isPublishing: Boolean = false,
) {
    val totalImagesCount: Int
        get() = existingImages.size + newImageUris.size

    val canPublish: Boolean
        get() = !isPublishing && (text.isNotBlank() || totalImagesCount > 0)
}