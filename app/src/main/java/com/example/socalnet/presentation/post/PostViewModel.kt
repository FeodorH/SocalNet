package com.example.socalnet.presentation.post

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.socalnet.core.common.AppError
import com.example.socalnet.core.common.AppResult
import com.example.socalnet.core.common.UiState
import com.example.socalnet.core.common.ValidationRules
import com.example.socalnet.core.model.ImageRef
import com.example.socalnet.core.model.PostDraft
import com.example.socalnet.domain.usecase.CreatePostUseCase
import com.example.socalnet.domain.usecase.GetPostUseCase
import com.example.socalnet.domain.usecase.UpdatePostUseCase
import com.example.socalnet.presentation.post.model.SavePostEvent
import com.example.socalnet.presentation.post.model.SavePostUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SavePostViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val createPostUseCase: CreatePostUseCase,
    private val updatePostUseCase: UpdatePostUseCase,
    private val getPostUseCase: GetPostUseCase,
) : ViewModel() {

    private val postId: String? = savedStateHandle["postId"]

    private val _state = MutableStateFlow<UiState<SavePostUiState>>(
        if (postId == null) {
            UiState.Content(SavePostUiState(existingPostId = null))
        } else {
            UiState.Loading
        }
    )
    val state: StateFlow<UiState<SavePostUiState>> = _state.asStateFlow()

    private val _events = Channel<SavePostEvent>(Channel.BUFFERED)
    val events: Flow<SavePostEvent> = _events.receiveAsFlow()

    init {
        if (postId != null) loadExistingPost(postId)
    }

    fun onTextChange(text: String) {
        if (text.length > ValidationRules.MAX_POST_TEXT) return
        _state.updateContent { it.copy(text = text) }
    }

    fun onImagesSelected(uris: List<String>) {
        _state.updateContent { current ->
            val available = ValidationRules.MAX_POST_IMAGES - current.totalImagesCount
            current.copy(newImageUris = current.newImageUris + uris.take(available))
        }
    }

    fun onNewImageRemoved(index: Int) {
        _state.updateContent { current ->
            current.copy(newImageUris = current.newImageUris.filterIndexed { i, _ -> i != index })
        }
    }

    fun onExistingImageRemoved(index: Int) {
        _state.updateContent { current ->
            current.copy(existingImages = current.existingImages.filterIndexed { i, _ -> i != index })
        }
    }

    fun save() {
        val current = _state.value
        if (current !is UiState.Content) return
        val s = current.data
        if (!s.canPublish) return

        viewModelScope.launch {
            _state.updateContent { it.copy(isPublishing = true) }

            val result = if (s.existingPostId == null) {
                createPostUseCase(
                    PostDraft(s.text.trim(), s.newImageUris.map { ImageRef.Local(it) })
                )
            } else {
                updatePostUseCase(
                    postId = s.existingPostId,
                    text = s.text.trim(),
                    existingImages = s.existingImages,
                    addedImages = s.newImageUris.map { ImageRef.Local(it) }
                )
            }

            when (result) {
                is AppResult.Success -> _events.send(SavePostEvent.Saved(result.data))
                is AppResult.Failure -> {
                    _state.updateContent { it.copy(isPublishing = false) }
                    _events.send(SavePostEvent.ShowError(result.error))
                }
            }
        }
    }

    private fun loadExistingPost(id: String) = viewModelScope.launch {
        when (val r = getPostUseCase(id)) {
            is AppResult.Success -> {
                _state.value = UiState.Content(
                    SavePostUiState(
                        text = r.data.text,
                        existingImages = r.data.images,
                        existingPostId = id,
                    )
                )
            }
            is AppResult.Failure -> {
                _state.value = UiState.Error(
                    message = when (r.error) {
                        AppError.NotFound -> "Пост не найден или удалён"
                        AppError.Network -> "Нет подключения к интернету"
                        else -> "Не удалось загрузить пост"
                    }
                )
            }
        }
    }
}

private inline fun <T> MutableStateFlow<UiState<T>>.updateContent(
    transform: (T) -> T,
) {
    update { current ->
        if (current is UiState.Content) UiState.Content(transform(current.data))
        else current
    }
}