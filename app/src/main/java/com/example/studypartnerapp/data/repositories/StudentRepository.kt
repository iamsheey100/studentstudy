package com.example.studypartnerapp.data.repositories

import com.example.studypartnerapp.data.models.Student
import com.example.studypartnerapp.firebase.FirebaseModule
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class StudentRepository(private val db: FirebaseDatabase = FirebaseModule.database) {
    private val studentsRef = db.getReference("students")

    suspend fun createStudentProfile(student: Student): Boolean {
        return try {
            studentsRef.child(student.uid).setValue(student).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getStudentProfile(uid: String): Student? {
        return try {
            val snapshot = studentsRef.child(uid).get().await()
            snapshot.getValue(Student::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateStudentProfile(student: Student): Boolean {
        return try {
            studentsRef.child(student.uid).setValue(student).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getAllStudents(): List<Student> {
        return try {
            val snapshot = studentsRef.get().await()
            snapshot.children.mapNotNull { it.getValue(Student::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
