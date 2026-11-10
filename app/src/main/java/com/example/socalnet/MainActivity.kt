package com.example.socalnet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.socalnet.core.common.AppResult
import com.example.socalnet.core.model.FeedType
import com.example.socalnet.core.ui.theme.SocalNetTheme
import com.example.socalnet.domain.repository.AuthRepository
import com.example.socalnet.domain.repository.FeedRepository
import com.example.socalnet.domain.repository.MediaRepository
import com.example.socalnet.domain.repository.PostRepository
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var auth: AuthRepository
    @Inject lateinit var posts: PostRepository
    @Inject lateinit var feeds: FeedRepository
    @Inject lateinit var media: MediaRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                SmokeTestScreen(
                    auth = auth, posts = posts, feeds = feeds, media = media
                )
            }
        }
    }

    @Composable
    fun SmokeTestScreen(
        auth: AuthRepository,
        posts: PostRepository,
        feeds: FeedRepository,
        media: MediaRepository,
    ) {
        val scope = rememberCoroutineScope()
        var log by remember { mutableStateOf("") }

        fun append(msg: String) { log = log + "\n" + msg }

        Column(Modifier.padding(16.dp)) {
            Button(onClick = {
                scope.launch {
                    append("=== Регистрация ===")
                    when (val r = auth.register("test${System.currentTimeMillis()}@test.com", "password123", "Тест")) {
                        is AppResult.Success -> append("✅ ${r.data.name} / ${r.data.id}")
                        is AppResult.Failure -> append("❌ ${r.error}")
                    }
                }
            }) { Text("1. Регистрация") }

            Button(onClick = {
                scope.launch {
                    append("=== Создание поста ===")
                    val userId = auth.currentUserId.first() ?: return@launch append("❌ Не авторизован")
                    when (val r = posts.createPost(userId, "Привет, мир!", emptyList())) {
                        is AppResult.Success -> append("✅ Post: ${r.data.id} от ${r.data.author.name}")
                        is AppResult.Failure -> append("❌ ${r.error}")
                    }
                }
            }) { Text("2. Создать пост") }

            Button(onClick = {
                scope.launch {
                    append("=== Общая лента ===")
                    when (val r = feeds.loadFeed(FeedType.GENERAL, null, 10)) {
                        is AppResult.Success -> {
                            append("✅ Постов: ${r.data.posts.size}")
                            r.data.posts.forEach { append("  • ${it.author.name}: ${it.text}") }
                        }
                        is AppResult.Failure -> append("❌ ${r.error}")
                    }
                }
            }) { Text("3. Загрузить ленту") }

            Spacer(Modifier.height(16.dp))
            Text(log, style = MaterialTheme.typography.bodySmall)
        }
    }
}