package com.example.studypartnerapp.data.models

data class Notification(
    val notificationId: String = "",
    val recipient: String = "",
    val message: String = "",
    val type: String = "", // request, match, system
    val relatedId: String = "",
    val read: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
