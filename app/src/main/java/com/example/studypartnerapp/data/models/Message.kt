package com.example.studypartnerapp.data.models

data class Message(
    val messageId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val text: String = "",
    val type: String = "text", // text, call_init, call_accept, call_decline
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
