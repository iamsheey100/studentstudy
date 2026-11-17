package com.example.studypartnerapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.studypartnerapp.data.models.Student
import com.example.studypartnerapp.ui.components.*
import com.example.studypartnerapp.navigation.Screen
import com.example.studypartnerapp.ui.theme.*
import com.example.studypartnerapp.utils.Resource
import com.example.studypartnerapp.viewmodels.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = viewModel()
) {
    val profileState by viewModel.profileState.collectAsState()
    val updateState by viewModel.updateState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var name by remember { mutableStateOf("") }
    var admissionNumber by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var course by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var campus by remember { mutableStateOf("") }
    var unitsString by remember { mutableStateOf("") }
    var studyMethod by remember { mutableStateOf("") }
    var availability by remember { mutableStateOf<List<String>>(emptyList()) }

    val courses = listOf(
        "Bachelor of Computer Science (BCS)",
        "Bachelor of Information Technology (BIT)",
        "Bachelor of Commerce (BCOM)",
        "Bachelor of Education (BEd)",
        "Bachelor of Engineering (BEng)",
        "Nursing",
        "Bachelor of Laws (LLB)",
        "Bachelor of Arts",
        "Sharia Law"
    )
    val years = listOf("Year 1", "Year 2", "Year 3", "Year 4", "Year 5")
    val studyMethods = listOf("Physical Meetings", "Online Meetings", "Hybrid", "Library Sessions", "Group Discussions", "One-on-One Sessions")

    LaunchedEffect(profileState) {
        if (profileState is Resource.Success) {
            val student = (profileState as Resource.Success<Student>).data!!
            name = student.name
            admissionNumber = student.admissionNumber
            phone = student.phone
            course = student.course
            year = student.year
            campus = student.campus
            unitsString = student.units.joinToString(", ")
            studyMethod = student.studyMethod
            availability = student.availability
        }
    }

    LaunchedEffect(updateState) {
        if (updateState is Resource.Success && (updateState as Resource.Success<Boolean>).data == true) {
            Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
        } else if (updateState is Resource.Error) {
            Toast.makeText(context, (updateState as Resource.Error).message, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile", fontWeight = FontWeight.ExtraBold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandDeepGreen)
            )
        },
        bottomBar = { AppBottomNavigation(navController) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (profileState is Resource.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                
                StudentAvatar(name = name, size = 100.dp, fontSize = 40)
                
                Spacer(modifier = Modifier.height(24.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 4.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        ExpertTextField(value = name, onValueChange = { name = it }, label = "Full Name", icon = Icons.Default.Person)
                        ExpertTextField(value = admissionNumber, onValueChange = { admissionNumber = it }, label = "Admission Number", icon = Icons.Default.Badge)
                        ExpertTextField(value = phone, onValueChange = { phone = it }, label = "Phone Number", icon = Icons.Default.Phone)
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        Surface(
                            color = BrandDeepGreen.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Academic Profile", 
                                fontWeight = FontWeight.ExtraBold, 
                                color = BrandDeepGreen,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        AppDropdownMenu(label = "Course", options = courses, selectedOption = course, onOptionSelected = { course = it })
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        AppDropdownMenu(label = "Year of Study", options = years, selectedOption = year, onOptionSelected = { year = it })
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        ExpertTextField(value = campus, onValueChange = { campus = it }, label = "Campus", icon = Icons.Default.Business)
                        
                        ExpertTextField(value = unitsString, onValueChange = { unitsString = it }, label = "Units (e.g. Web, OOP, Java)", icon = Icons.Default.ListAlt)
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        AppDropdownMenu(label = "Study Method", options = studyMethods, selectedOption = studyMethod, onOptionSelected = { studyMethod = it })
                        Spacer(modifier = Modifier.height(16.dp))

                        AvailabilityPicker(
                            selectedOptions = availability,
                            onOptionToggled = { option ->
                                availability = if (availability.contains(option)) {
                                    availability - option
                                } else {
                                    availability + option
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        PrimaryButton(
                            text = "Save Changes",
                            onClick = {
                                val currentStudent = (profileState as? Resource.Success<Student>)?.data
                                if (currentStudent != null) {
                                    val unitsList = unitsString.split(",")
                                        .map { it.trim() }
                                        .filter { it.isNotEmpty() }
                                        
                                    val updatedStudent = currentStudent.copy(
                                        name = name,
                                        admissionNumber = admissionNumber,
                                        phone = phone,
                                        course = course,
                                        year = year,
                                        campus = campus,
                                        units = unitsList,
                                        studyMethod = studyMethod,
                                        availability = availability,
                                        updatedAt = System.currentTimeMillis()
                                    )
                                    viewModel.updateProfile(updatedStudent)
                                }
                            },
                            isLoading = updateState is Resource.Loading,
                            containerColor = BrandGreen
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                TextButton(onClick = { 
                    viewModel.logout()
                    navController.navigate(com.example.studypartnerapp.navigation.Screen.Login.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                }) {
                    Text("Logout", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
