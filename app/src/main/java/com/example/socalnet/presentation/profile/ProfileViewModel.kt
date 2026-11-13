package com.example.socalnet.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.socalnet.core.common.AppResult
import com.example.socalnet.core.common.UiState
import com.example.socalnet.core.model.ImageRef
import com.example.socalnet.core.model.Post
import com.example.socalnet.domain.repository.remote.MediaRepository
import com.example.socalnet.domain.usecase.DeletePostUseCase
import com.example.socalnet.domain.usecase.LogoutUseCase
import com.example.socalnet.domain.usecase.ObserveCurrentUserUseCase
import com.example.socalnet.domain.usecase.ObserveUserPostsUseCase
import com.example.socalnet.domain.usecase.SyncCurrentUserUseCase
import com.example.socalnet.domain.usecase.SyncUserPostsUseCase
import com.example.socalnet.domain.usecase.UpdateProfileUseCase
import com.example.socalnet.presentation.profile.model.ProfileUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val observeCurrentUser: ObserveCurrentUserUseCase,
    private val observeUserPosts: ObserveUserPostsUseCase,
    private val syncCurrentUser: SyncCurrentUserUseCase,
    private val syncUserPosts: SyncUserPostsUseCase,
    private val updateProfile: UpdateProfileUseCase,
    private val deletePostUc: DeletePostUseCase,
    private val logoutUc: LogoutUseCase,
    private val mediaRepository: MediaRepository,
) : ViewModel() {

    private val _isRefreshingPosts = MutableStateFlow(false)
    private val _isUpdatingProfile = MutableStateFlow(false)
    private val _isUploadingAvatar = MutableStateFlow(false)

    private val _events = Channel<ProfileEvent>(Channel.BUFFERED)
    val events: Flow<ProfileEvent> = _events.receiveAsFlow()

    // Посты текущего пользователя (empty если нет user или постов)
    private val postsFlow: Flow<List<Post>> = observeCurrentUser()
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList())
            else observeUserPosts(user.id)
        }

    val state: StateFlow<UiState<ProfileUiState>> = combine(
        observeCurrentUser(),
        postsFlow,
        _isRefreshingPosts,
        _isUpdatingProfile,
        _isUploadingAvatar,
    ) { user, posts, refreshing, updating, uploading ->
        if (user == null) {
            // Нет пользователя — либо Loading (проверяем токен), либо Guest.
            // Guest-сценарий отсекается на уровне MainNavHost (role == GUEST → GuestProfilePrompt).
            UiState.Loading
        } else {
            UiState.Content(
                ProfileUiState(
                    user = user,
                    posts = posts,
                    isRefreshingPosts = refreshing,
                    isUpdatingProfile = updating,
                    isUploadingAvatar = uploading,
                )
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UiState.Loading,
    )

    init {
        // Первая загрузка: профиль синхронизируем «молча», посты — с индикатором
        viewModelScope.launch {
            val userId = observeCurrentUser().first()?.id ?: return@launch

            syncCurrentUser()              // быстро, кэш → сеть

            _isRefreshingPosts.value = true
            syncUserPosts(userId)
            _isRefreshingPosts.value = false
        }
    }

    // ============ ДЕЙСТВИЯ ============

    /**
     * Pull-to-refresh — только посты (как ты и хотел).
     * Профиль обновится после updateProfile-операций или при следующем входе.
     */
    fun refresh() = viewModelScope.launch {
        val userId = (state.value as? UiState.Content)?.data?.user?.id ?: return@launch
        _isRefreshingPosts.value = true
        when (val r = syncUserPosts(userId)) {
            is AppResult.Success -> Unit
            is AppResult.Failure -> _events.send(ProfileEvent.ShowError(r.error))
        }
        _isRefreshingPosts.value = false
    }

    fun updateNameAndBio(name: String, bio: String) = viewModelScope.launch {
        _isUpdatingProfile.value = true
        when (val r = updateProfile(name = name, bio = bio, avatarUrl = null, avatarPublicId = null)) {
            is AppResult.Success -> _events.send(ProfileEvent.ShowMessage("Профиль обновлён"))
            is AppResult.Failure -> _events.send(ProfileEvent.ShowError(r.error))
        }
        _isUpdatingProfile.value = false
    }

    fun updateAvatar(localUri: String) = viewModelScope.launch {
        val userId = (state.value as? UiState.Content)?.data?.user?.id ?: return@launch
        _isUploadingAvatar.value = true

        when (val uploadResult = mediaRepository.upload(
            ImageRef.Local(localUri),
            path = "socialnet/avatars/$userId/${System.currentTimeMillis()}"
        )) {
            is AppResult.Success -> {
                when (val r = updateProfile(
                    name = null,
                    bio = null,
                    avatarUrl = uploadResult.data.imageRef.url,
                    avatarPublicId = uploadResult.data.imageRef.publicId,
                )) {
                    is AppResult.Success -> _events.send(ProfileEvent.ShowMessage("Аватар обновлён"))
                    is AppResult.Failure -> {
                        // Компенсация: удаляем загруженный файл
                        uploadResult.data.deleteToken?.let { mediaRepository.deleteByToken(it) }
                        _events.send(ProfileEvent.ShowError(r.error))
                    }
                }
            }
            is AppResult.Failure -> _events.send(ProfileEvent.ShowError(uploadResult.error))
        }
        _isUploadingAvatar.value = false
    }

    fun removeAvatar() = viewModelScope.launch {
        _isUpdatingProfile.value = true
        when (val r = updateProfile(
            name = null, bio = null, avatarUrl = null, avatarPublicId = null, removeAvatar = true
        )) {
            is AppResult.Success -> _events.send(ProfileEvent.ShowMessage("Аватар удалён"))
            is AppResult.Failure -> _events.send(ProfileEvent.ShowError(r.error))
        }
        _isUpdatingProfile.value = false
    }

    fun deletePost(postId: String) = viewModelScope.launch {
        when (val r = deletePostUc(postId)) {
            is AppResult.Success -> _events.send(ProfileEvent.ShowMessage("Пост удалён"))
            is AppResult.Failure -> _events.send(ProfileEvent.ShowError(r.error))
        }
    }

    fun logout() = viewModelScope.launch {
        when (val r = logoutUc()) {
            is AppResult.Success -> _events.send(ProfileEvent.LoggedOut)
            is AppResult.Failure -> _events.send(ProfileEvent.ShowError(r.error))
        }
    }
}

sealed interface ProfileEvent {
    data class ShowMessage(val text: String) : ProfileEvent
    data class ShowError(val error: com.example.socalnet.core.common.AppError) : ProfileEvent
    data object LoggedOut : ProfileEvent
}