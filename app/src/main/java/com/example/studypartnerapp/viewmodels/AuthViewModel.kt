package com.example.studypartnerapp.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studypartnerapp.data.models.Student
import com.example.studypartnerapp.data.repositories.AuthRepository
import com.example.studypartnerapp.data.repositories.StudentRepository
import com.example.studypartnerapp.utils.Resource
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val studentRepository: StudentRepository = StudentRepository()
) : ViewModel() {

    private val TAG = "KCA_AUTH"

    val currentUser: StateFlow<FirebaseUser?> = authRepository.currentUser
    val authStateInitialized: StateFlow<Boolean> = authRepository.authStateInitialized

    private val _authState = MutableStateFlow<Resource<FirebaseUser>>(Resource.Loading())
    val authState: StateFlow<Resource<FirebaseUser>> = _authState

    init {
        // Initialize authState with a non-loading state if no user is present
        if (authRepository.currentUser.value == null) {
            _authState.value = Resource.Error("Not logged in") // Or a custom Initial state
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            Log.d(TAG, "Attempting login for: $email")
            _authState.value = Resource.Loading()
            authRepository.login(email, password) { user, error ->
                if (user != null) {
                    Log.d(TAG, "Login successful")
                    _authState.value = Resource.Success(user)
                } else {
                    Log.e(TAG, "Login failed: $error")
                    _authState.value = Resource.Error(error ?: "Login failed")
                }
            }
        }
    }

    fun register(student: Student, password: String) {
        viewModelScope.launch {
            Log.d(TAG, "Starting registration for: ${student.email}")
            _authState.value = Resource.Loading()
            
            authRepository.register(student.email, password) { user, error ->
                if (user != null) {
                    val uid = user.uid
                    Log.d(TAG, "Auth successful, UID: $uid")
                    
                    val updatedStudent = student.copy(uid = uid)
                    
                    // Launch a new coroutine on the viewModelScope to handle profile creation
                    viewModelScope.launch {
                        try {
                            Log.d(TAG, "Attempting to create profile in RTDB...")
                            val profileCreated = studentRepository.createStudentProfile(updatedStudent)
                            
                            if (profileCreated) {
                                Log.d(TAG, "Profile created successfully!")
                                _authState.value = Resource.Success(user)
                            } else {
                                Log.e(TAG, "Profile creation failed in repository")
                                _authState.value = Resource.Error("Could not save profile data. Please check connection.")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Exception during profile creation: ${e.message}")
                            _authState.value = Resource.Error("Database Error: ${e.localizedMessage}")
                        }
                    }
                } else {
                    Log.e(TAG, "Auth registration failed: $error")
                    _authState.value = Resource.Error(error ?: "Registration failed")
                }
            }
        }
    }

    fun logout() {
        Log.d(TAG, "Logging out user")
        authRepository.logout()
    }
}
