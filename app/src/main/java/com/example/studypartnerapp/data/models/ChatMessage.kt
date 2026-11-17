package com.example.studypartnerapp.data.models

data class ChatMessage(
    val messageId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
