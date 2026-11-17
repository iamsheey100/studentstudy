package com.example.studypartnerapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.PeopleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.studypartnerapp.navigation.Screen
import com.example.studypartnerapp.ui.components.AppBottomNavigation
import com.example.studypartnerapp.ui.components.EmptyStateView
import com.example.studypartnerapp.ui.components.StudentAvatar
import com.example.studypartnerapp.ui.theme.*
import com.example.studypartnerapp.utils.Resource
import com.example.studypartnerapp.viewmodels.ChatViewModel
import com.example.studypartnerapp.viewmodels.MatchViewModel
import com.example.studypartnerapp.viewmodels.MatchWithStudent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchesScreen(
    navController: NavController,
    viewModel: MatchViewModel = viewModel(),
    chatViewModel: ChatViewModel = viewModel()
) {
    val matchesState by viewModel.matches.collectAsState()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Accepted Matches", fontWeight = FontWeight.ExtraBold, color = Color.White) },
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
            when (matchesState) {
                is Resource.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.primary)
                }
                is Resource.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = (matchesState as Resource.Error).message ?: "Error")
                    }
                }
                is Resource.Success -> {
                    val matches = (matchesState as Resource.Success<List<MatchWithStudent>>).data!!
                    if (matches.isEmpty()) {
                        EmptyStateView(
                            message = "No Matches Yet",
                            description = "Start searching for partners to see your matches here.",
                            icon = Icons.Default.PeopleOutline
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(matches) { matchWithStudent ->
                                MatchCard(
                                    matchWithStudent = matchWithStudent,
                                    onRemove = { viewModel.removeMatch(matchWithStudent.match.matchId) },
                                    onChat = {
                                        scope.launch {
                                            val chatId = chatViewModel.startChat(
                                                matchWithStudent.student.uid,
                                                matchWithStudent.student.name
                                            )
                                            navController.navigate(
                                                Screen.Chat.createRoute(
                                                    chatId,
                                                    matchWithStudent.student.uid,
                                                    matchWithStudent.student.name
                                                )
                                            )
                                        }
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
fun MatchCard(matchWithStudent: MatchWithStudent, onRemove: () -> Unit, onChat: () -> Unit) {
    val student = matchWithStudent.student
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = student.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (student.status == "Available") SuccessGreen else ErrorRed)
                        )
                    }
                    Text(text = student.course, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Badge {
                    Text("${matchWithStudent.match.score}% Match")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = student.email, fontSize = 14.sp)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onChat,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandGreen,
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Chat", color = Color.White, fontWeight = FontWeight.ExtraBold)
                }
                
                Button(
                    onClick = onRemove,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ErrorRed,
                        contentColor = Color.White
                    )
                ) {
                    Text("Remove", color = Color.White, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}
