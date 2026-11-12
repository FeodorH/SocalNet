package com.example.socalnet.presentation.common

sealed interface UiEvent {
    data class ShowMessage(val text: String) : UiEvent
}