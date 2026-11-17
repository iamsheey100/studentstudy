package com.example.studypartnerapp.data.models

data class Chat(
    val chatId: String = "",
    val participants: Map<String, Boolean> = emptyMap(),
    val lastMessage: String = "",
    val lastMessageTimestamp: Long = 0,
    val unreadBy: List<String> = emptyList() // List of UIDs who haven't read the last message
)
