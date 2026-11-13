package com.example.socalnet.presentation.post.model

import com.example.socalnet.core.common.AppError
import com.example.socalnet.core.model.Post

sealed interface SavePostEvent {
    /** Пост успешно создан или обновлён — можно закрывать экран. */
    data class Saved(val post: Post) : SavePostEvent

    /** Ошибка сохранения — показать snackbar. */
    data class ShowError(val error: AppError) : SavePostEvent
}