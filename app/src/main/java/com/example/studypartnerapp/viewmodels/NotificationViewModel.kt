package com.example.studypartnerapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studypartnerapp.data.models.Notification
import com.example.studypartnerapp.data.repositories.StudentRepository
import com.example.studypartnerapp.data.repositories.AuthRepository
import com.example.studypartnerapp.data.repositories.NotificationRepository
import com.example.studypartnerapp.data.repositories.StudyRequestRepository
import com.example.studypartnerapp.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NotificationViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val notificationRepository: NotificationRepository = NotificationRepository(),
    private val requestRepository: StudyRequestRepository = StudyRequestRepository(),
    private val studentRepository: StudentRepository = StudentRepository()
) : ViewModel() {

    private val _notifications = MutableStateFlow<Resource<List<Notification>>>(Resource.Loading())
    val notifications: StateFlow<Resource<List<Notification>>> = _notifications

    fun loadData() {
        val currentUid = authRepository.currentUser.value?.uid ?: return
        viewModelScope.launch {
            _notifications.value = Resource.Loading()
            val savedNotifications = notificationRepository.getNotificationsForStudent(currentUid)
                .filterNot { it.type == "request" }
            val requestNotifications = requestRepository.getRequestsForStudent(currentUid).map { request ->
                val senderName = studentRepository.getStudentProfile(request.from)
                    ?.name
                    ?.ifBlank { "a student" }
                    ?: "a student"
                Notification(
                    notificationId = "request:${request.requestId}",
                    recipient = currentUid,
                    message = "Study request from $senderName. Tap to review it.",
                    type = "study_request",
                    relatedId = request.requestId,
                    timestamp = request.timestamp
                )
            }
            _notifications.value = Resource.Success(
                (savedNotifications + requestNotifications).sortedByDescending { it.timestamp }
            )
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.markAsRead(notificationId)
            loadData()
        }
    }

    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.deleteNotification(notificationId)
            loadData()
        }
    }
}
