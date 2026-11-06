package com.example.socalnet.core.utils

import java.util.UUID

object IdGenerator {
    fun generate() : String =
        UUID.randomUUID().toString()
}