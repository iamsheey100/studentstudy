package com.example.studypartnerapp.data.models

data class StudyGroup(
    val groupId: String = "",
    val groupName: String = "",
    val unit: String = "",
    val meetingTime: String = "",
    val createdBy: String = "",
    val members: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)
