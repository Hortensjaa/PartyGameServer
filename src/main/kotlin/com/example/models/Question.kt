package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class Question (
    val header: Headers,
    val text: String
)

enum class Headers {
    WHO {
        override fun toString() = "Who..."
    },
    SAY {
        override fun toString() = "Who said that?"
    },
    DO {
        override fun toString() = "Who is more likely to...?"
    },
}