package com.example.socalnet

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.socalnet.core.common.AppResult
import com.example.socalnet.core.model.FeedType
import com.example.socalnet.core.model.ImageRef
import com.example.socalnet.core.model.PostDraft
import com.example.socalnet.core.model.ReactionType
import com.example.socalnet.domain.repository.common.*
import com.example.socalnet.domain.repository.remote.MediaRepository
import com.example.socalnet.domain.usecase.CreatePostUseCase
import com.example.socalnet.domain.usecase.ToggleReactionUseCase
import com.example.socalnet.domain.usecase.ToggleSubscriptionUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "SMOKE"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var auth: OfflineFirstAuthRepository
    @Inject lateinit var posts: OfflineFirstPostRepository
    @Inject lateinit var feeds: OfflineFirstFeedRepository
    @Inject lateinit var subscriptions: OfflineFirstSubscriptionRepository
    @Inject lateinit var reactions: OfflineFirstReactionRepository
    @Inject lateinit var media: MediaRepository
    @Inject lateinit var createPost: CreatePostUseCase
    @Inject lateinit var toggleReaction: ToggleReactionUseCase
    @Inject lateinit var toggleSubscription: ToggleSubscriptionUseCase

    // Сохраняем между кликами для сценария
    private var testEmail: String = ""
    private val testPassword = "password123"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme { SmokeTestContent() }
        }
    }

    @Composable
    private fun SmokeTestContent() {
        val scope = rememberCoroutineScope()
        val log = remember { mutableStateListOf<String>() }
        val scroll = rememberScrollState()

        // Автоскролл вниз при новом сообщении
        LaunchedEffect(log.size) { scroll.animateScrollTo(scroll.maxValue) }

        fun append(msg: String) {
            log.add(msg)
            Log.d(TAG, msg)
        }

        fun runBlock(name: String, block: suspend () -> Unit) {
            scope.launch {
                append("▶ $name")
                try { block() } catch (e: Exception) {
                    append("❌ Exception: ${e.message}")
                    Log.e(TAG, "Exception in $name", e)
                }
            }
        }

        Column(Modifier.fillMaxSize().padding(16.dp)) {
            // --- Кнопки ---
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Button(onClick = {
                    runBlock("Register") {
                        testEmail = "smoke${System.currentTimeMillis()}@test.com"
                        when (val r = auth.register(testEmail, testPassword, "Smoke")) {
                            is AppResult.Success -> append("✅ Registered: ${r.data.name} (${r.data.id})")
                            is AppResult.Failure -> append("❌ ${r.error}")
                        }
                    }
                }) { Text("1.Reg") }

                Button(onClick = {
                    runBlock("Login") {
                        if (testEmail.isEmpty()) return@runBlock append("❌ Сначала Register")
                        when (val r = auth.login(testEmail, testPassword)) {
                            is AppResult.Success -> append("✅ Login: ${r.data.name}")
                            is AppResult.Failure -> append("❌ ${r.error}")
                        }
                    }
                }) { Text("2.Login") }

                Button(onClick = {
                    runBlock("CreatePost") {
                        val draft = PostDraft("Smoke post ${System.currentTimeMillis()}", emptyList())
                        when (val r = createPost(draft)) {
                            is AppResult.Success -> append("✅ Post: ${r.data.id}, author=${r.data.author.name}")
                            is AppResult.Failure -> append("❌ ${r.error}")
                        }
                    }
                }) { Text("3.Post") }
            }

            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Button(onClick = {
                    runBlock("LoadFeed") {
                        val userId = auth.currentUserId.first() ?: return@runBlock append("❌ Not logged")
                        // 1. Сначала Room (мгновенно, может быть 0)
                        val cachedBefore = feeds.observeFeed(FeedType.GENERAL, userId).first()
                        append("  Room cache before sync: ${cachedBefore.size}")
                        // 2. Сеть
                        when (val r = feeds.syncFeed(FeedType.GENERAL, null, 20)) {
                            is AppResult.Success -> append("  Sync OK, nextCursor=${r.data != null}")
                            is AppResult.Failure -> append("  ❌ Sync: ${r.error}")
                        }
                        // 3. Room после sync
                        val cachedAfter = feeds.observeFeed(FeedType.GENERAL, userId).first()
                        append("✅ Room after sync: ${cachedAfter.size} posts")
                        cachedAfter.take(3).forEach { append("    • ${it.author.name}: ${it.text.take(30)}") }
                    }
                }) { Text("4.Feed") }

                Button(onClick = {
                    runBlock("Subscribe") {
                        val userId = auth.currentUserId.first() ?: return@runBlock append("❌ Not logged")
                        val cached = feeds.observeFeed(FeedType.GENERAL, userId).first()
                        val target = cached.firstOrNull { it.author.id != userId }?.author
                            ?: return@runBlock append("❌ Нет чужих постов в ленте")
                        val already = subscriptions.observeIsSubscribed(userId, target.id).first()
                        when (val r = toggleSubscription(target.id, already)) {
                            is AppResult.Success -> append("✅ ${if (already) "Unsub" else "Sub"}: ${target.name}")
                            is AppResult.Failure -> append("❌ ${r.error}")
                        }
                    }
                }) { Text("5.Sub") }

                Button(onClick = {
                    runBlock("React") {
                        val userId = auth.currentUserId.first() ?: return@runBlock append("❌ Not logged")
                        val cached = feeds.observeFeed(FeedType.GENERAL, userId).first()
                        val target = cached.firstOrNull() ?: return@runBlock append("❌ Нет постов")
                        val current = reactions.observeMyReaction(target.id).first()
                        when (val r = toggleReaction(target.id, ReactionType.LIKE, current)) {
                            is AppResult.Success -> append("✅ React: total=${r.data.total}")
                            is AppResult.Failure -> append("❌ ${r.error}")
                        }
                    }
                }) { Text("6.React") }
            }

            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Button(onClick = {
                    runBlock("RefreshAll") {
                        val userId = auth.currentUserId.first() ?: return@runBlock append("❌ Not logged")
                        auth.syncCurrentUser().let {
                            append(if (it is AppResult.Success) "  ✅ Profile synced" else "  ❌ Profile: ${(it as AppResult.Failure).error}")
                        }
                        subscriptions.syncSubscriptions(userId).let {
                            append(if (it is AppResult.Success) "  ✅ Subscriptions synced" else "  ❌ Subs: ${(it as AppResult.Failure).error}")
                        }
                        feeds.syncFeed(FeedType.GENERAL, null, 20).let {
                            append(if (it is AppResult.Success) "✅ Feed synced" else "❌ Feed: ${(it as AppResult.Failure).error}")
                        }
                    }
                }) { Text("7.Refresh") }

                Button(onClick = {
                    runBlock("Logout") {
                        when (val r = auth.logout()) {
                            is AppResult.Success -> append("✅ Logged out (Room очищен)")
                            is AppResult.Failure -> append("❌ ${r.error}")
                        }
                    }
                }) { Text("8.Logout") }

                Button(onClick = { log.clear() }) { Text("Clear") }
            }

            Spacer(Modifier.height(16.dp))

            // --- Лог ---
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxSize()
            ) {
                Column(Modifier.padding(8.dp).verticalScroll(scroll)) {
                    log.forEach { line ->
                        Text(line, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}