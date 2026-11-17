package com.example.studypartnerapp.data.models

data class StudyRequest(
    val requestId: String = "",
    val from: String = "",
    val to: String = "",
    val status: String = "pending", // pending, accepted, rejected
    val timestamp: Long = System.currentTimeMillis()
)

data class RequestWithStudent(
    val request: StudyRequest,
    val student: Student
)
