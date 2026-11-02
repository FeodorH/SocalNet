package com.example.socalnet.core.model

enum class ReactionType { LIKE, LOVE, FIRE, WOW, SAD }

data class ReactionSummary(
    val total: Int = 0,
    val byType: Map<ReactionType, Int> = emptyMap(),
) {
    companion object { val EMPTY = ReactionSummary() }
}