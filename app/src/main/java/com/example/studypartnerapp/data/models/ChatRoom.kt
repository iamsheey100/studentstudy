package com.example.studypartnerapp.data.models

data class ChatRoom(
    val chatId: String = "",
    val participants: Map<String, Boolean> = emptyMap(),
    val participantNames: Map<String, String> = emptyMap(), // Added to store all participant names
    val lastMessage: String = "",
    val lastTimestamp: Long = System.currentTimeMillis(),
    val lastSenderId: String = "",
    val unreadCount: Map<String, Int> = emptyMap(),
    val otherParticipantPhoto: String? = null,
    val isGroup: Boolean = false,
    val groupName: String? = null
)
