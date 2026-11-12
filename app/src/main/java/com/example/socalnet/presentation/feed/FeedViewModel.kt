package com.example.socalnet.presentation.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.socalnet.core.common.AppResult
import com.example.socalnet.core.common.UiState
import com.example.socalnet.core.model.FeedType
import com.example.socalnet.core.model.PageCursor
import com.example.socalnet.core.model.Post
import com.example.socalnet.core.model.ReactionType
import com.example.socalnet.domain.usecase.ObserveCurrentUserUseCase
import com.example.socalnet.domain.usecase.ObserveFeedUseCase
import com.example.socalnet.domain.usecase.SyncFeedUseCase
import com.example.socalnet.domain.usecase.ToggleReactionUseCase
import com.example.socalnet.presentation.common.UiEvent
import com.example.socalnet.presentation.feed.model.FeedUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.emptyList

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class FeedViewModel @Inject constructor(
    private val observeFeed: ObserveFeedUseCase,
    private val syncFeedUseCase: SyncFeedUseCase,
    private val toggleReaction: ToggleReactionUseCase,
    private val observeCurrentUser: ObserveCurrentUserUseCase,
) : ViewModel() {

    /** управляющие флаги */
    private val _selectedTab = MutableStateFlow(FeedType.GENERAL)
    private val _isRefreshing = MutableStateFlow(false)
    private val _isLoadingMore = MutableStateFlow(false)
    private val _hasMore = MutableStateFlow(true)
    private val _initialAttemptCompleted = MutableStateFlow(false)
    private val _initialError = MutableStateFlow<String?>(null)

    private var cursor: PageCursor? = null

    /** События (Snackbar и т.п.) */
    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events: Flow<UiEvent> = _events.receiveAsFlow()

    private val feedFlow: Flow<List<Post>> =
        _selectedTab.flatMapLatest {
            flow{
                emit(emptyList())
                emitAll(observeFeed(it))
            }
        }

    val state: StateFlow<UiState<FeedUiState>> = combine(
        combine(feedFlow, _selectedTab) { posts, tab -> posts to tab },
        combine(_isRefreshing, _isLoadingMore) { r, l -> r to l },
        combine(_hasMore, _initialAttemptCompleted) { h, a -> h to a },
        _initialError,
    ) { (posts, tab), (refreshing, loadingMore), (hasMore, completed), error ->

        val feedState = FeedUiState(
            selectedTab = tab,
            posts = posts,
            isRefreshing = refreshing,
            isLoadingMore = loadingMore,
            hasMore = hasMore,
        )

        /**
         * Логика выбора UiState:
         * 1. Есть посты → Content (даже если есть ошибка/идёт refresh)
         * 2. Первая попытка не завершилась → Loading
         * 3. Попытка завершилась с ошибкой и без постов → Error
         * 4. Попытка завершилась успешно, но постов нет → Empty
         */
        when {
            posts.isNotEmpty() -> UiState.Content(feedState)
            !completed -> UiState.Loading
            error != null -> UiState.Error(error)
            else -> UiState.Empty
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UiState.Loading,
    )

    init {
        performSync(_selectedTab.value, cursor = null)

        viewModelScope.launch {
            observeCurrentUser().collect { user ->
                if (user == null && _selectedTab.value == FeedType.SUBSCRIPTIONS) {
                    selectTab(FeedType.GENERAL)
                }
            }
        }
    }

    fun selectTab(tab: FeedType) = viewModelScope.launch {
        if (tab == _selectedTab.value) return@launch

        if (tab == FeedType.SUBSCRIPTIONS && observeCurrentUser().first() == null) {
            _events.send(UiEvent.ShowMessage("Войдите, чтобы увидеть подписки"))
            return@launch
        }

        _selectedTab.value = tab
        cursor = null
        _hasMore.value = true
        _initialAttemptCompleted.value = false
        _initialError.value = null

        performSync(tab, cursor = null)
    }

    fun refresh() {
        cursor = null
        _hasMore.value = true
        performSync(_selectedTab.value, cursor = null)
    }

    fun loadMore() {
        if (_isRefreshing.value || _isLoadingMore.value) return
        val c = cursor ?: return
        if (!_hasMore.value) return
        performSync(_selectedTab.value, c)
    }

    fun onReactionClick(postId: String, type: ReactionType) = viewModelScope.launch {
        val current = state.value
        if (current !is UiState.Content) return@launch

        val post = current.data.posts.find { it.id == postId } ?: return@launch

        when (val r = toggleReaction(postId, type, post.myReaction)) {
            is AppResult.Success -> {

            }
            is AppResult.Failure -> {
                _events.send(UiEvent.ShowMessage("Не удалось изменить реакцию"))
            }
        }
    }

    private fun performSync(type: FeedType, cursor: PageCursor?) = viewModelScope.launch {
        if (cursor == null) {
            _isRefreshing.value = true
        } else {
            _isLoadingMore.value = true
        }

        when (val result = syncFeedUseCase(type, cursor)) {
            is AppResult.Success -> {
                this@FeedViewModel.cursor = result.data
                _hasMore.value = result.data != null
                _initialAttemptCompleted.value = true
                _initialError.value = null
            }
            is AppResult.Failure -> {
                _initialAttemptCompleted.value = true
                if (state.value is UiState.Content) {
                    _events.send(UiEvent.ShowMessage("Не удалось загрузить: ${result.error}"))
                } else {
                    _initialError.value = result.error.toString()
                }
            }
        }

        _isRefreshing.value = false
        _isLoadingMore.value = false
    }
}