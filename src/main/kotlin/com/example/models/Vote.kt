package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class Vote(
    val player: String,
    val vote: String
)
