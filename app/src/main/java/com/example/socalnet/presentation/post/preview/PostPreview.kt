package com.example.socalnet.presentation.post.preview

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.socalnet.core.common.UiState
import com.example.socalnet.core.model.ImageRef
import com.example.socalnet.presentation.post.SavePostScreen
import com.example.socalnet.presentation.post.model.SavePostUiState

@Preview(showBackground = true, name = "Create · пусто")
@Composable
private fun SavePostCreateEmptyPreview() {
    MaterialTheme {
        SavePostScreen(
            state = UiState.Content(SavePostUiState()),
            onTextChange = {},
            onImagesSelected = {},
            onNewImageRemoved = {},
            onExistingImageRemoved = {},
            onSave = {},
            onBack = {},
            onRetryLoad = {},
        )
    }
}

@Preview(showBackground = true, name = "Create · с текстом")
@Composable
private fun SavePostCreateWithTextPreview() {
    MaterialTheme {
        SavePostScreen(
            state = UiState.Content(
                SavePostUiState(text = "Сегодня отличный день для нового поста ☀️")
            ),
            onTextChange = {},
            onImagesSelected = {},
            onNewImageRemoved = {},
            onExistingImageRemoved = {},
            onSave = {},
            onBack = {},
            onRetryLoad = {},
        )
    }
}

@Preview(showBackground = true, name = "Edit · существующий пост")
@Composable
private fun SavePostEditPreview() {
    MaterialTheme {
        SavePostScreen(
            state = UiState.Content(
                SavePostUiState(
                    text = "Мой существующий пост",
                    existingImages = listOf(
                        ImageRef.Remote("https://example.com/img1.jpg", "pid1"),
                    ),
                    existingPostId = "post_123",
                )
            ),
            onTextChange = {},
            onImagesSelected = {},
            onNewImageRemoved = {},
            onExistingImageRemoved = {},
            onSave = {},
            onBack = {},
            onRetryLoad = {},
        )
    }
}

@Preview(showBackground = true, name = "Publishing · идёт сохранение")
@Composable
private fun SavePostPublishingPreview() {
    MaterialTheme {
        SavePostScreen(
            state = UiState.Content(
                SavePostUiState(text = "Публикую...", isPublishing = true)
            ),
            onTextChange = {},
            onImagesSelected = {},
            onNewImageRemoved = {},
            onExistingImageRemoved = {},
            onSave = {},
            onBack = {},
            onRetryLoad = {},
        )
    }
}

@Preview(showBackground = true, name = "Loading · загрузка поста")
@Composable
private fun SavePostLoadingPreview() {
    MaterialTheme {
        SavePostScreen(
            state = UiState.Loading,
            onTextChange = {},
            onImagesSelected = {},
            onNewImageRemoved = {},
            onExistingImageRemoved = {},
            onSave = {},
            onBack = {},
            onRetryLoad = {},
        )
    }
}

@Preview(showBackground = true, name = "Error · пост не найден")
@Composable
private fun SavePostErrorPreview() {
    MaterialTheme {
        SavePostScreen(
            state = UiState.Error("Пост не найден или удалён"),
            onTextChange = {},
            onImagesSelected = {},
            onNewImageRemoved = {},
            onExistingImageRemoved = {},
            onSave = {},
            onBack = {},
            onRetryLoad = {},
        )
    }
}