package com.example.fastvlm05b.model

import android.net.Uri

enum class Role {
    USER, ASSISTANT, SYSTEM
}

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val role: Role,
    val timestamp: Long = System.currentTimeMillis(),
    val imageUri: Uri? = null
)
