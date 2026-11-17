package com.example.studypartnerapp.data.models

data class Match(
    val matchId: String = "",
    val student1: String = "",
    val student2: String = "",
    val score: Int = 0,
    val status: String = "accepted", // accepted, removed
    val createdAt: Long = System.currentTimeMillis()
)
