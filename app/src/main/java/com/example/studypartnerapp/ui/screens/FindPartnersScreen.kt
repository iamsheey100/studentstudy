package com.example.studypartnerapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.studypartnerapp.navigation.Screen
import com.example.studypartnerapp.ui.components.*
import com.example.studypartnerapp.ui.theme.*
import com.example.studypartnerapp.utils.Resource
import com.example.studypartnerapp.viewmodels.PartnerMatch
import com.example.studypartnerapp.viewmodels.PartnerViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FindPartnersScreen(
    navController: NavController,
    viewModel: PartnerViewModel = viewModel()
) {
    val partnersState by viewModel.partners.collectAsState()
    val requestStatus by viewModel.requestStatus.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search Partners", fontWeight = FontWeight.ExtraBold, color = Color.White) },
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
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (partnersState) {
                is Resource.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.primary)
                }
                is Resource.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = (partnersState as Resource.Error).message ?: "Error",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                is Resource.Success -> {
                    val partners = (partnersState as Resource.Success<List<PartnerMatch>>).data!!
                    if (partners.isEmpty()) {
                        EmptyStateView(
                            message = "No Partners Found",
                            description = "Try adjusting your profile or preferences to find more students.",
                            icon = Icons.Default.Search
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(partners) { partnerMatch ->
                                PartnerCard(
                                    partnerMatch = partnerMatch,
                                    isRequested = requestStatus[partnerMatch.student.uid] ?: false,
                                    navController = navController,
                                    onSendRequest = { viewModel.sendRequest(partnerMatch.student.uid) },
                                    onViewProfile = {
                                        navController.navigate(Screen.PartnerProfile.createRoute(partnerMatch.student.uid))
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PartnerCard(
    partnerMatch: PartnerMatch,
    isRequested: Boolean,
    navController: NavController,
    onSendRequest: () -> Unit,
    onViewProfile: () -> Unit
) {
    val student = partnerMatch.student
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StudentAvatar(name = student.name, size = 50.dp)
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = student.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = student.course,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Badge {
                    Text("${partnerMatch.score}% Match")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Common Units:",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = student.units.joinToString(", ").ifEmpty { "None listed" },
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSendRequest,
                    enabled = !isRequested,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandDeepGreen,
                        contentColor = Color.White,
                        disabledContainerColor = BrandDeepGreen.copy(alpha = 0.5f),
                        disabledContentColor = Color.White.copy(alpha = 0.7f)
                    )
                ) {
                    Text(
                        text = if (isRequested) "Requested" else "Send Request",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }

                Button(
                    onClick = onViewProfile,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E293B), // Dark Theme Color
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Profile", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }
}
