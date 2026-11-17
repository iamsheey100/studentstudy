package com.example.studypartnerapp.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studypartnerapp.data.models.Notification
import com.example.studypartnerapp.data.models.Student
import com.example.studypartnerapp.data.models.StudyRequest
import com.example.studypartnerapp.data.repositories.AuthRepository
import com.example.studypartnerapp.data.repositories.NotificationRepository
import com.example.studypartnerapp.data.repositories.StudentRepository
import com.example.studypartnerapp.data.repositories.StudyRequestRepository
import com.example.studypartnerapp.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PartnerViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val studentRepository: StudentRepository = StudentRepository(),
    private val requestRepository: StudyRequestRepository = StudyRequestRepository(),
    private val notificationRepository: NotificationRepository = NotificationRepository()
) : ViewModel() {

    private val _partners = MutableStateFlow<Resource<List<PartnerMatch>>>(Resource.Loading())
    val partners: StateFlow<Resource<List<PartnerMatch>>> = _partners

    private val _requestStatus = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val requestStatus: StateFlow<Map<String, Boolean>> = _requestStatus

    init {
        findPartners()
    }

    fun findPartners() {
        val currentUid = authRepository.currentUser.value?.uid ?: return
        viewModelScope.launch {
            _partners.value = Resource.Loading()
            try {
                val allStudents = studentRepository.getAllStudents()
                val currentStudent = allStudents.find { it.uid == currentUid }
                
                if (currentStudent != null) {
                    val partnerMatches = allStudents
                        .filter { it.uid != currentUid }
                        .map { other ->
                            PartnerMatch(other, calculateMatchScore(currentStudent, other))
                        }
                        .sortedByDescending { it.score }
                    
                    _partners.value = Resource.Success(partnerMatches)
                } else {
                    _partners.value = Resource.Error("Please complete your profile first to find partners.")
                }
            } catch (e: Exception) {
                _partners.value = Resource.Error("Failed to fetch students: ${e.message}")
            }
        }
    }

    private fun calculateMatchScore(s1: Student, s2: Student): Int {
        var score = 0
        
        // Same Unit: 50% (Score proportional to how many units match)
        val s1Units = s1.units.map { it.lowercase().trim() }.toSet()
        val s2Units = s2.units.map { it.lowercase().trim() }.toSet()
        val commonUnits = s1Units.intersect(s2Units)
        
        if (commonUnits.isNotEmpty()) {
            score += 50
        }
        
        // Same Course: 30%
        if (s1.course.isNotBlank() && s1.course.equals(s2.course, ignoreCase = true)) {
            score += 30
        }
        
        // Similar Availability: 20%
        val commonAvailability = s1.availability.intersect(s2.availability.toSet())
        if (commonAvailability.isNotEmpty()) {
            score += 20
        }
        
        return score
    }

    fun sendRequest(toStudentId: String) {
        val fromUid = authRepository.currentUser.value?.uid ?: return
        viewModelScope.launch {
            try {
                val request = StudyRequest(
                    from = fromUid,
                    to = toStudentId,
                    status = "pending",
                    timestamp = System.currentTimeMillis()
                )
                val success = requestRepository.sendStudyRequest(request)
                if (success) {
                    // Update UI state for this button
                    _requestStatus.value = _requestStatus.value + (toStudentId to true)
                    
                    // Also send a notification
                    val notification = Notification(
                        recipient = toStudentId,
                        message = "New study request from a potential partner!",
                        type = "request",
                        relatedId = fromUid,
                        timestamp = System.currentTimeMillis()
                    )
                    if (!notificationRepository.sendNotification(notification)) {
                        Log.e(
                            "PartnerViewModel",
                            "Study request was saved, but its notification could not be saved."
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("PartnerViewModel", "Error sending request: ${e.message}")
            }
        }
    }
}

data class PartnerMatch(
    val student: Student,
    val score: Int
)
