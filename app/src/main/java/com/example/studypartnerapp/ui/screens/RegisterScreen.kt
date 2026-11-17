package com.example.studypartnerapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import com.example.studypartnerapp.navigation.Screen
import com.example.studypartnerapp.ui.components.*
import com.example.studypartnerapp.ui.theme.*
import com.example.studypartnerapp.utils.Resource
import com.example.studypartnerapp.viewmodels.AuthViewModel

@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: AuthViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var admissionNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    // New fields for comprehensive profile
    var course by remember { mutableStateOf("Bachelor of Computer Science (BCS)") }
    var year by remember { mutableStateOf("Year 1") }
    var campus by remember { mutableStateOf("") }
    var unitsString by remember { mutableStateOf("") }
    var studyMethod by remember { mutableStateOf("Physical Meetings") }
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

    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    LaunchedEffect(authState) {
        if (authState is Resource.Success) {
            navController.navigate(Screen.Dashboard.route) {
                popUpTo(Screen.Register.route) { inclusive = true }
            }
        } else if (authState is Resource.Error) {
            val error = (authState as Resource.Error).message
            if (error != "Not logged in") {
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BrandDeepGreen
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Create Account",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                text = "Join our community of students",
                color = Color.White.copy(alpha = 0.85f),
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(32.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    ExpertTextField(value = name, onValueChange = { name = it }, label = "Full Name", icon = Icons.Default.Person)
                    ExpertTextField(value = email, onValueChange = { email = it }, label = "Institutional Email", icon = Icons.Default.Email)
                    ExpertTextField(value = phone, onValueChange = { phone = it }, label = "Phone Number", icon = Icons.Default.Phone)
                    ExpertTextField(value = admissionNumber, onValueChange = { admissionNumber = it }, label = "Admission Number", icon = Icons.Default.Badge)
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    Surface(
                        color = BrandGreen.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Academic Details", 
                            fontWeight = FontWeight.ExtraBold, 
                            color = BrandGreen,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    AppDropdownMenu(label = "Course", options = courses, selectedOption = course, onOptionSelected = { course = it })
                    Spacer(modifier = Modifier.height(8.dp))
                    AppDropdownMenu(label = "Year of Study", options = years, selectedOption = year, onOptionSelected = { year = it })
                    Spacer(modifier = Modifier.height(8.dp))
                    ExpertTextField(value = campus, onValueChange = { campus = it }, label = "Campus", icon = Icons.Default.Business)
                    ExpertTextField(value = unitsString, onValueChange = { unitsString = it }, label = "Units (e.g. Web, OOP, Java)", icon = Icons.Default.ListAlt)
                    
                    AppDropdownMenu(label = "Study Method", options = studyMethods, selectedOption = studyMethod, onOptionSelected = { studyMethod = it })
                    Spacer(modifier = Modifier.height(8.dp))

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

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Color.LightGray.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(16.dp))

                    ExpertTextField(value = password, onValueChange = { password = it }, label = "Secure Password", icon = Icons.Default.Lock, isPassword = true)
                    ExpertTextField(value = confirmPassword, onValueChange = { confirmPassword = it }, label = "Confirm Password", icon = Icons.Default.Lock, isPassword = true)

                    Spacer(modifier = Modifier.height(32.dp))

                    PrimaryButton(
                        text = "Create My Account",
                        onClick = {
                            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || 
                                admissionNumber.isEmpty() || password.isEmpty() || campus.isEmpty()) {
                                Toast.makeText(context, "Please fill all mandatory fields", Toast.LENGTH_SHORT).show()
                            } else if (password != confirmPassword) {
                                Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                            } else {
                                val unitsList = unitsString.split(",")
                                    .map { it.trim() }
                                    .filter { it.isNotEmpty() }

                                val student = Student(
                                    name = name,
                                    email = email,
                                    phone = phone,
                                    admissionNumber = admissionNumber,
                                    course = course,
                                    year = year,
                                    campus = campus,
                                    units = unitsList,
                                    studyMethod = studyMethod,
                                    availability = availability
                                )
                                viewModel.register(student, password)
                            }
                        },
                        isLoading = authState is Resource.Loading,
                        containerColor = BrandGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(onClick = { navController.popBackStack() }) {
                Text(
                    "Already have an account? Login",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
