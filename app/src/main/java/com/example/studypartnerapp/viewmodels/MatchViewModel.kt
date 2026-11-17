package com.example.studypartnerapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studypartnerapp.data.models.Match
import com.example.studypartnerapp.data.models.Student
import com.example.studypartnerapp.data.repositories.AuthRepository
import com.example.studypartnerapp.data.repositories.MatchRepository
import com.example.studypartnerapp.data.repositories.StudentRepository
import com.example.studypartnerapp.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MatchViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val matchRepository: MatchRepository = MatchRepository(),
    private val studentRepository: StudentRepository = StudentRepository()
) : ViewModel() {

    private val _matches = MutableStateFlow<Resource<List<MatchWithStudent>>>(Resource.Loading())
    val matches: StateFlow<Resource<List<MatchWithStudent>>> = _matches

    init {
        loadMatches()
    }

    fun loadMatches() {
        val currentUid = authRepository.currentUser.value?.uid ?: return
        viewModelScope.launch {
            _matches.value = Resource.Loading()
            val matchesList = matchRepository.getMatchesForStudent(currentUid)
            val matchesWithStudents = matchesList.map { match ->
                val otherId = if (match.student1 == currentUid) match.student2 else match.student1
                val otherStudent = studentRepository.getStudentProfile(otherId)
                    ?: Student(uid = otherId, name = "Student")
                MatchWithStudent(match, otherStudent)
            }
            _matches.value = Resource.Success(matchesWithStudents)
        }
    }

    fun removeMatch(matchId: String) {
        viewModelScope.launch {
            val success = matchRepository.removeMatch(matchId)
            if (success) {
                loadMatches()
            }
        }
    }
}

data class MatchWithStudent(
    val match: Match,
    val student: Student
)
