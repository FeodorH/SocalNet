package com.example.socalnet.presentation.post

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.socalnet.core.common.UiState
import com.example.socalnet.core.common.ValidationRules
import com.example.socalnet.core.model.ImageRef
import com.example.socalnet.presentation.post.model.SavePostUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavePostScreen(
    state: UiState<SavePostUiState>,
    onTextChange: (String) -> Unit,
    onImagesSelected: (List<String>) -> Unit,
    onNewImageRemoved: (Int) -> Unit,
    onExistingImageRemoved: (Int) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
    onRetryLoad: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val content = (state as? UiState.Content)?.data
    val isEdit = content?.existingPostId != null

    // Photo Picker — без разрешений, работает на API 33+, fallback на API 21+
    val pickImagesLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(
            maxItems = (ValidationRules.MAX_POST_IMAGES - (content?.totalImagesCount ?: 0))
                .coerceAtLeast(1)
        )
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) onImagesSelected(uris.map { it.toString() })
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "Редактировать" else "Создать пост") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть")
                    }
                },
                actions = {
                    if (content != null) {
                        if (content.isPublishing) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .padding(end = 16.dp)
                                    .size(24.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            TextButton(
                                onClick = onSave,
                                enabled = content.canPublish,
                            ) { Text("Опубликовать") }
                        }
                    }
                },
            )
        },
    ) { padding ->
        when (state) {
            UiState.Loading -> FullScreenLoading(Modifier.padding(padding))
            UiState.Empty -> FullScreenMessage(
                message = "Пост не найден",
                actionText = "Повторить",
                onAction = onRetryLoad,
                modifier = Modifier.padding(padding),
            )
            is UiState.Error -> FullScreenMessage(
                message = state.message,
                actionText = if (state.retryable) "Повторить" else null,
                onAction = onRetryLoad,
                modifier = Modifier.padding(padding),
            )
            is UiState.Content -> FormContent(
                state = state.data,
                onTextChange = onTextChange,
                onNewImageRemoved = onNewImageRemoved,
                onExistingImageRemoved = onExistingImageRemoved,
                onAddImagesClick = {
                    pickImagesLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@Composable
private fun FormContent(
    state: SavePostUiState,
    onTextChange: (String) -> Unit,
    onNewImageRemoved: (Int) -> Unit,
    onExistingImageRemoved: (Int) -> Unit,
    onAddImagesClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(
            value = state.text,
            onValueChange = onTextChange,
            placeholder = { Text("Что нового?") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 5,
            maxLines = 12,
            enabled = !state.isPublishing,
            supportingText = {
                Text(
                    "${state.text.length} / ${ValidationRules.MAX_POST_TEXT}",
                    style = MaterialTheme.typography.bodySmall,
                )
            },
        )

        if (state.totalImagesCount > 0) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Уже загруженные картинки (только в edit)
                itemsIndexed(state.existingImages) { index, image ->
                    ImageThumbnail(
                        model = image.url,
                        onRemove = { onExistingImageRemoved(index) },
                        enabled = !state.isPublishing,
                    )
                }
                // Новые локальные
                itemsIndexed(state.newImageUris) { index, uri ->
                    ImageThumbnail(
                        model = uri,
                        onRemove = { onNewImageRemoved(index) },
                        enabled = !state.isPublishing,
                    )
                }
                if (state.totalImagesCount < ValidationRules.MAX_POST_IMAGES) {
                    item {
                        AddImageThumbnail(
                            enabled = !state.isPublishing,
                            onClick = onAddImagesClick,
                        )
                    }
                }
            }
            Text(
                "Изображений: ${state.totalImagesCount} / ${ValidationRules.MAX_POST_IMAGES}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            OutlinedButton(
                onClick = onAddImagesClick,
                enabled = !state.isPublishing,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Добавить фото")
            }
        }
    }
}

@Composable
private fun ImageThumbnail(
    model: String,
    onRemove: () -> Unit,
    enabled: Boolean,
) {
    Box(
        modifier = Modifier
            .size(96.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        AsyncImage(
            model = model,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        IconButton(
            onClick = onRemove,
            enabled = enabled,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(24.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                    shape = CircleShape,
                ),
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Удалить",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun AddImageThumbnail(
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(96.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.Default.Add,
            contentDescription = "Добавить",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun FullScreenLoading(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun FullScreenMessage(
    message: String,
    actionText: String?,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(message, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        if (actionText != null) {
            Spacer(Modifier.height(12.dp))
            Button(onClick = onAction) { Text(actionText) }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SavePostCreateEmptyPreview() {
    MaterialTheme {
        SavePostScreen(
            state = UiState.Content(SavePostUiState()),
            onTextChange = {}, onImagesSelected = {},
            onNewImageRemoved = {}, onExistingImageRemoved = {},
            onSave = {}, onBack = {}, onRetryLoad = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SavePostCreateWithTextPreview() {
    MaterialTheme {
        SavePostScreen(
            state = UiState.Content(
                SavePostUiState(text = "Сегодня отличный день для нового поста ☀️")
            ),
            onTextChange = {}, onImagesSelected = {},
            onNewImageRemoved = {}, onExistingImageRemoved = {},
            onSave = {}, onBack = {}, onRetryLoad = {},
        )
    }
}

@Preview(showBackground = true)
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
            onTextChange = {}, onImagesSelected = {},
            onNewImageRemoved = {}, onExistingImageRemoved = {},
            onSave = {}, onBack = {}, onRetryLoad = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SavePostPublishingPreview() {
    MaterialTheme {
        SavePostScreen(
            state = UiState.Content(
                SavePostUiState(text = "Публикую...", isPublishing = true)
            ),
            onTextChange = {}, onImagesSelected = {},
            onNewImageRemoved = {}, onExistingImageRemoved = {},
            onSave = {}, onBack = {}, onRetryLoad = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SavePostLoadingPreview() {
    MaterialTheme {
        SavePostScreen(
            state = UiState.Loading,
            onTextChange = {}, onImagesSelected = {},
            onNewImageRemoved = {}, onExistingImageRemoved = {},
            onSave = {}, onBack = {}, onRetryLoad = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SavePostErrorPreview() {
    MaterialTheme {
        SavePostScreen(
            state = UiState.Error("Пост не найден или удалён"),
            onTextChange = {}, onImagesSelected = {},
            onNewImageRemoved = {}, onExistingImageRemoved = {},
            onSave = {}, onBack = {}, onRetryLoad = {},
        )
    }
}