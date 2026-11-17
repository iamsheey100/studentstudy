package com.example.studypartnerapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studypartnerapp.data.models.Student
import com.example.studypartnerapp.data.repositories.AuthRepository
import com.example.studypartnerapp.data.repositories.StudentRepository
import com.example.studypartnerapp.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val studentRepository: StudentRepository = StudentRepository()
) : ViewModel() {

    private val _profileState = MutableStateFlow<Resource<Student>>(Resource.Loading())
    val profileState: StateFlow<Resource<Student>> = _profileState

    private val _updateState = MutableStateFlow<Resource<Boolean>>(Resource.Success(false))
    val updateState: StateFlow<Resource<Boolean>> = _updateState

    init {
        loadProfile()
    }

    fun loadProfile() {
        val uid = authRepository.currentUser.value?.uid ?: return
        viewModelScope.launch {
            _profileState.value = Resource.Loading()
            val profile = studentRepository.getStudentProfile(uid)
            if (profile != null) {
                _profileState.value = Resource.Success(profile)
            } else {
                _profileState.value = Resource.Error("Profile not found")
            }
        }
    }

    fun updateProfile(student: Student) {
        viewModelScope.launch {
            _updateState.value = Resource.Loading()
            val success = studentRepository.updateStudentProfile(student)
            if (success) {
                _updateState.value = Resource.Success(true)
                _profileState.value = Resource.Success(student)
            } else {
                _updateState.value = Resource.Error("Update failed")
            }
        }
    }

    fun logout() {
        authRepository.logout()
    }
}
