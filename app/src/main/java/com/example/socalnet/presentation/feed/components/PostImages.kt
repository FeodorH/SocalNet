package com.example.socalnet.presentation.feed.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.socalnet.core.model.ImageRef

@Composable
fun PostImages(
    images: List<ImageRef.Remote>,
    modifier: Modifier = Modifier,
) {
    if (images.isEmpty()) return

    val shape = RoundedCornerShape(12.dp)

    when (images.size) {
        1 -> {
            AsyncImage(
                model = images[0].url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )
        }
        2 -> {
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                images.forEach { img ->
                    ImageCell(
                        url = img.url,
                        modifier = Modifier.weight(1f).aspectRatio(1f),
                        shape = shape,
                    )
                }
            }
        }
        3 -> {
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                ImageCell(
                    url = images[0].url,
                    modifier = Modifier.weight(2f).aspectRatio(2f / 3f),
                    shape = shape,
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    ImageCell(
                        url = images[1].url,
                        modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                        shape = shape,
                    )
                    ImageCell(
                        url = images[2].url,
                        modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                        shape = shape,
                    )
                }
            }
        }
        else -> {  // 4+
            Column(
                modifier = modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    ImageCell(
                        url = images[0].url,
                        modifier = Modifier.weight(1f).aspectRatio(1f),
                        shape = shape,
                    )
                    ImageCell(
                        url = images[1].url,
                        modifier = Modifier.weight(1f).aspectRatio(1f),
                        shape = shape,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    ImageCell(
                        url = images[2].url,
                        modifier = Modifier.weight(1f).aspectRatio(1f),
                        shape = shape,
                    )
                    ImageCell(
                        url = images[3].url,
                        modifier = Modifier.weight(1f).aspectRatio(1f),
                        shape = shape,
                        overlayCount = if (images.size > 4) images.size - 4 else null,
                    )
                }
            }
        }
    }
}

@Composable
private fun ImageCell(
    url: String,
    modifier: Modifier,
    shape: Shape,
    overlayCount: Int? = null,
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        AsyncImage(
            model = url,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        if (overlayCount != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "+$overlayCount",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium,
                )
            }
        }
    }
}