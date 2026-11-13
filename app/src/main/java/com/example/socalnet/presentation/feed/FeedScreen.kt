package com.example.socalnet.presentation.feed

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.socalnet.core.common.UiState
import com.example.socalnet.core.model.FeedType
import com.example.socalnet.core.model.ReactionType
import com.example.socalnet.presentation.feed.components.PostCard
import com.example.socalnet.presentation.feed.model.FeedUiState

@Composable
fun FeedScreen(
    state: UiState<FeedUiState>,
    role: Boolean,
    onTabSelected: (FeedType) -> Unit,
    onReactionClick: (postId: String, type: ReactionType) -> Unit,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize()) {

        val selectedTab = (state as? UiState.Content)?.data?.selectedTab ?: FeedType.GENERAL

        TabRow(selectedTabIndex = selectedTab.ordinal) {
            Tab(
                selected = selectedTab == FeedType.GENERAL,
                onClick = { onTabSelected(FeedType.GENERAL) },
                text = { Text("Общая") },
            )
            Tab(
                selected = selectedTab == FeedType.SUBSCRIPTIONS,
                onClick = { onTabSelected(FeedType.SUBSCRIPTIONS) },
                enabled = role,
                text = { Text("Подписки") },
            )
        }

        when (state) {
            UiState.Loading -> LoadingState()
            UiState.Empty -> EmptyState(
                message = "Здесь пока пусто",
                hint = if (role) "Подпишитесь на кого-нибудь или создайте первый пост"
                else "Войдите, чтобы подписаться на авторов",
            )
            is UiState.Error -> ErrorState(
                message = state.message,
                onRetry = onRefresh,
            )
            is UiState.Content -> ContentState(
                state = state.data,
                isUser = role,
                onReactionClick = onReactionClick,
                onRefresh = onRefresh,
                onLoadMore = onLoadMore,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContentState(
    state: FeedUiState,
    isUser: Boolean,
    onReactionClick: (postId: String, type: ReactionType) -> Unit,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
) {
    val listState = rememberLazyListState()

    // Триггер пагинации: когда пользователь приблизился к концу
    LaunchedEffect(listState, state.posts.size) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisible ->
                // Порог: 3 элемента до конца — начинаем грузить
                if (lastVisible != null
                    && lastVisible >= state.posts.size - 3
                    && state.hasMore
                    && !state.isLoadingMore
                    && !state.isRefreshing
                ) {
                    onLoadMore()
                }
            }
    }

    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
        ) {
            items(state.posts, key = { it.id }) { post ->
                PostCard(
                    post = post,
                    enabled = isUser,
                    onReactionClick = { type -> onReactionClick(post.id, type) },
                )
            }
            if (state.isLoadingMore) {
                item {
                    Box(
                        Modifier.fillMaxWidth().padding(16.dp),
                        contentAlignment = Alignment.Center,
                    ) { CircularProgressIndicator() }
                }
            }
            if (!state.hasMore && state.posts.isNotEmpty()) {
                item {
                    Box(
                        Modifier.fillMaxWidth().padding(16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "Больше постов нет",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyState(message: String, hint: String) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(message, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            hint,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(message, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))
        Button(onClick = onRetry) { Text("Повторить") }
    }
}
