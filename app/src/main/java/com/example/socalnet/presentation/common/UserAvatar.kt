package com.example.socalnet.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun UserAvatar(
    name: String,
    avatarUrl: String?,
    size: Dp = 40.dp,
    modifier: Modifier = Modifier,
) {
    val shape = CircleShape
    if (avatarUrl.isNullOrBlank()) {
        Box(
            modifier = modifier
                .size(size)
                .clip(shape)
                .background(colorForName(name)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = name.take(1).uppercase().ifBlank { "?" },
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
            )
        }
    } else {
        AsyncImage(
            model = avatarUrl,
            contentDescription = "Аватар $name",
            contentScale = ContentScale.Crop,
            modifier = modifier
                .size(size)
                .clip(shape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        )
    }
}

private fun colorForName(name: String): Color {
    val palette = listOf(
        Color(0xFFEF5350), Color(0xFFAB47BC), Color(0xFF5C6BC0),
        Color(0xFF29B6F6), Color(0xFF26A69A), Color(0xFF9CCC65),
        Color(0xFFFFA726), Color(0xFF8D6E63),
    )
    return palette[(name.hashCode() and 0x7FFFFFFF) % palette.size]
}