package com.example.studypartnerapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studypartnerapp.data.models.Match
import com.example.studypartnerapp.data.models.Notification
import com.example.studypartnerapp.data.models.RequestWithStudent
import com.example.studypartnerapp.data.models.Student
import com.example.studypartnerapp.data.repositories.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val studentRepository: StudentRepository = StudentRepository(),
    private val matchRepository: MatchRepository = MatchRepository(),
    private val requestRepository: StudyRequestRepository = StudyRequestRepository(),
    private val groupRepository: GroupRepository = GroupRepository(),
    private val notificationRepository: NotificationRepository = NotificationRepository()
) : ViewModel() {

    private val _studentProfile = MutableStateFlow<Student?>(null)
    val studentProfile: StateFlow<Student?> = _studentProfile

    private val _stats = MutableStateFlow(DashboardStats())
    val stats: StateFlow<DashboardStats> = _stats

    private val _recentNotifications = MutableStateFlow<List<Notification>>(emptyList())
    val recentNotifications: StateFlow<List<Notification>> = _recentNotifications

    private val _pendingRequests = MutableStateFlow<List<RequestWithStudent>>(emptyList())
    val pendingRequests: StateFlow<List<RequestWithStudent>> = _pendingRequests

    private val _recommendations = MutableStateFlow<List<com.example.studypartnerapp.viewmodels.PartnerMatch>>(emptyList())
    val recommendations: StateFlow<List<com.example.studypartnerapp.viewmodels.PartnerMatch>> = _recommendations

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        val uid = authRepository.currentUser.value?.uid ?: return
        
        viewModelScope.launch {
            // Load Profile
            val profile = studentRepository.getStudentProfile(uid)
            _studentProfile.value = profile
            
            // Load Stats
            val matches = matchRepository.getMatchesForStudent(uid)
            val requests = requestRepository.getRequestsForStudent(uid)
            val groups = groupRepository.getAllGroups().filter { it.members.contains(uid) }
            val notifications = notificationRepository.getNotificationsForStudent(uid)
            
            _stats.value = DashboardStats(
                totalMatches = matches.size,
                pendingRequests = requests.size,
                groupsJoined = groups.size,
                unreadNotifications = notifications.count { !it.read && it.type != "request" } + requests.size
            )
            
            // Load Notifications
            _recentNotifications.value = notifications.take(5)

            // Load Pending Requests for Activity Feed
            val requestsWithStudents = requests.map { req ->
                val sender = studentRepository.getStudentProfile(req.from)
                    ?: Student(uid = req.from, name = "Student")
                RequestWithStudent(req, sender)
            }
            _pendingRequests.value = requestsWithStudents

            // Fetch Recommendations
            val allStudents = studentRepository.getAllStudents()
            if (profile != null) {
                val recommended = allStudents
                    .filter { it.uid != uid }
                    .map { other ->
                        com.example.studypartnerapp.viewmodels.PartnerMatch(other, calculateMatchScore(profile, other))
                    }
                    .sortedByDescending { it.score }
                    .take(3)
                _recommendations.value = recommended
            }
        }
    }

    fun acceptRequest(requestWithStudent: RequestWithStudent) {
        val currentUid = authRepository.currentUser.value?.uid ?: return
        val request = requestWithStudent.request
        viewModelScope.launch {
            val updated = requestRepository.updateRequestStatus(request.requestId, "accepted")
            if (updated) {
                matchRepository.createMatch(Match(student1 = request.from, student2 = currentUid, score = 100))
                notificationRepository.sendNotification(Notification(
                    recipient = request.from,
                    message = "Your study request was accepted!",
                    type = "match",
                    relatedId = currentUid
                ))
                loadDashboardData()
            }
        }
    }

    fun rejectRequest(requestId: String) {
        viewModelScope.launch {
            requestRepository.updateRequestStatus(requestId, "rejected")
            loadDashboardData()
        }
    }

    private fun calculateMatchScore(s1: Student, s2: Student): Int {
        var score = 0
        val s1Units = s1.units.map { it.lowercase().trim() }.toSet()
        val s2Units = s2.units.map { it.lowercase().trim() }.toSet()
        if (s1Units.intersect(s2Units).isNotEmpty()) score += 50
        if (s1.course.isNotBlank() && s1.course.equals(s2.course, ignoreCase = true)) score += 30
        if (s1.availability.intersect(s2.availability.toSet()).isNotEmpty()) score += 20
        return score
    }

    fun logout() {
        authRepository.logout()
    }

    fun updateStatus(newStatus: String) {
        val uid = authRepository.currentUser.value?.uid ?: return
        viewModelScope.launch {
            val currentProfile = _studentProfile.value ?: return@launch
            val updatedProfile = currentProfile.copy(status = newStatus)
            studentRepository.updateStudentProfile(updatedProfile)
            _studentProfile.value = updatedProfile
        }
    }

    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.deleteNotification(notificationId)
            loadDashboardData()
        }
    }
}

data class DashboardStats(
    val totalMatches: Int = 0,
    val pendingRequests: Int = 0,
    val groupsJoined: Int = 0,
    val unreadNotifications: Int = 0
)
