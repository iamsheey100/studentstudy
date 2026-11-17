package com.example.studypartnerapp.data.models

data class Student(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val admissionNumber: String = "",
    val phone: String = "", // Added Phone/Contact Field
    val course: String = "",
    val year: String = "",
    val campus: String = "",
    val units: List<String> = emptyList(),
    val interests: List<String> = emptyList(),
    val studyMethod: String = "",
    val availability: List<String> = emptyList(),
    val status: String = "Available", // Added: Available or Busy
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
