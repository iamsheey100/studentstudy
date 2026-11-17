package com.example.studypartnerapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.studypartnerapp.data.models.Student
import com.example.studypartnerapp.data.repositories.StudentRepository
import com.example.studypartnerapp.navigation.Screen
import com.example.studypartnerapp.ui.components.*
import com.example.studypartnerapp.ui.theme.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PartnerProfileViewModel(private val repository: StudentRepository = StudentRepository()) : ViewModel() {
    private val _student = MutableStateFlow<Student?>(null)
    val student: StateFlow<Student?> = _student

    fun loadStudent(uid: String) {
        viewModelScope.launch {
            _student.value = repository.getStudentProfile(uid)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PartnerProfileScreen(
    userId: String,
    navController: NavController,
    viewModel: PartnerProfileViewModel = viewModel()
) {
    val student by viewModel.student.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(userId) {
        viewModel.loadStudent(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Student Details", fontWeight = FontWeight.ExtraBold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandDeepGreen)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8FAFC))
        ) {
            student?.let { s ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    StudentAvatar(name = s.name, size = 120.dp, fontSize = 48)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = s.name,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BrandGreen
                    )
                    
                    Text(
                        text = s.course,
                        fontSize = 16.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    PortalInfoSection(title = "Academic Registry") {
                        PortalInfoRow(icon = Icons.Default.Badge, label = "Student ID", value = s.admissionNumber)
                        PortalInfoRow(icon = Icons.Default.School, label = "Year", value = s.year)
                        PortalInfoRow(icon = Icons.Default.Business, label = "Campus", value = s.campus)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    PortalInfoSection(title = "Learning Preferences") {
                        PortalInfoRow(icon = Icons.AutoMirrored.Filled.MenuBook, label = "Preferred Method", value = s.studyMethod)
                        PortalInfoRow(icon = Icons.Default.List, label = "Active Units", value = s.units.joinToString(", "))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    PortalInfoSection(title = "Weekly Availability") {
                        Text(
                            text = s.availability.joinToString(" • "),
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(40.dp))
                }
            } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BrandGreen)
            }
        }
    }
}

@Composable
fun PortalInfoSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            color = BrandGreen,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        PortalCard(backgroundColor = BrandGreen) {
            content()
        }
    }
}

@Composable
fun PortalInfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(modifier = Modifier.size(36.dp), shape = RoundedCornerShape(8.dp), color = Color.White.copy(alpha = 0.2f)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = label, fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
            Text(text = value, fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.ExtraBold)
        }
    }
}
