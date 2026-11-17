package com.example.studypartnerapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import com.example.studypartnerapp.navigation.Screen
import com.example.studypartnerapp.data.models.RequestWithStudent
import com.example.studypartnerapp.ui.components.AppBottomNavigation
import com.example.studypartnerapp.ui.components.StudentAvatar
import com.example.studypartnerapp.ui.theme.*
import com.example.studypartnerapp.viewmodels.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = viewModel()
) {
    val student by viewModel.studentProfile.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val notifications by viewModel.recentNotifications.collectAsState()
    val pendingRequests by viewModel.pendingRequests.collectAsState()
    val recommendations by viewModel.recommendations.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text("KCA HUB", fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp, color = Color.White)
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Notifications.route) }) {
                        BadgedBox(
                            badge = {
                                if (stats.unreadNotifications > 0) {
                                    Badge(containerColor = BrandGreen) { Text(stats.unreadNotifications.toString(), color = Color.White) }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = "Alerts", tint = Color.White)
                        }
                    }
                    IconButton(onClick = { navController.navigate(Screen.About.route) }) {
                        Icon(Icons.Default.Info, contentDescription = "About", tint = Color.White)
                    }
                    IconButton(onClick = { 
                        viewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(navController.graph.id) { inclusive = true }
                            launchSingleTop = true
                        }
                    }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BrandDeepGreen
                )
            )
        },
        bottomBar = { AppBottomNavigation(navController) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                
                // Top-Left Navigation Section with Names
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TopNavButton("Groups", Icons.Default.Groups, BrandGreen) {
                        navController.navigate(Screen.Groups.route)
                    }
                    TopNavButton("About", Icons.Default.AutoStories, BrandDeepGreen) {
                        navController.navigate(Screen.About.route)
                    }
                    TopNavButton("Help", Icons.Default.HelpCenter, Color(0xFFF59E0B)) {
                        /* Help Dialog */
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                // Welcome Section (Brand Gradient)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(BrandGreen, BrandLightGold)
                                )
                            )
                            .padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Welcome back,",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = student?.name?.split(" ")?.firstOrNull() ?: "Student",
                                color = Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        StudentAvatar(
                            name = student?.name ?: "",
                            size = 56.dp,
                            fontSize = 24,
                            modifier = Modifier.clickable { navController.navigate(Screen.Profile.route) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // New Feature: Student Status Toggle
                LiveStatusCard(
                    currentStatus = student?.status ?: "Available",
                    onStatusChange = { viewModel.updateStatus(it) }
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Modern Stats Grid
                Text(
                    text = "Academic Summary",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = BrandGreen
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ModernStatCard("Matches", stats.totalMatches.toString(), Icons.Default.Diversity3, BrandGold, Modifier.weight(1f))
                    ModernStatCard(
                        "Requests",
                        stats.pendingRequests.toString(),
                        Icons.Default.PendingActions,
                        Color(0xFFF59E0B),
                        Modifier.weight(1f).clickable {
                            coroutineScope.launch { listState.animateScrollToItem(1) }
                        }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ModernStatCard("Joined Groups", stats.groupsJoined.toString(), Icons.Default.Groups, BrandGreen, Modifier.weight(1f))
                    ModernStatCard(
                        "New Alerts",
                        stats.unreadNotifications.toString(),
                        Icons.Default.Notifications,
                        Color(0xFFEF4444),
                        Modifier.weight(1f).clickable {
                            navController.navigate(Screen.Notifications.route)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Recommended Partners Section
                if (recommendations.isNotEmpty()) {
                    Text(
                        text = "Suggested Partners",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BrandGreen
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 8.dp)
                    ) {
                        items(recommendations) { match ->
                            RecommendedPartnerCard(match) {
                                navController.navigate(Screen.FindPartners.route)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }

                // Unit Learning Resources Section (Highlighted Section)
                if (student?.units?.isNotEmpty() == true) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(28.dp),
                        color = Color(0xFFF1F5F9), // Professional Silver/Light Grey background
                        shadowElevation = 0.dp,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Unit Learning Resources",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = BrandGreen
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                contentPadding = PaddingValues(bottom = 8.dp)
                            ) {
                                items(student?.units ?: emptyList()) { unit ->
                                    UnitResourceCard(
                                        unitName = unit,
                                        onClick = {
                                            navController.navigate(Screen.UnitResources.createRoute(unit))
                                        }
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Study Tips Carousel
                Text(
                    text = "Quick Tips",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = BrandGreen
                )
                
                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    item { TipCard("Active Recall", "Testing yourself is 50% more effective than reading.", Icons.Default.Bolt, BrandGreen) }
                    item { TipCard("Pomodoro", "Study for 25 mins, break for 5. Stay fresh!", Icons.Default.Timer, BrandGold) }
                    item { TipCard("Peer Review", "Explaining concepts helps you master them faster.", Icons.Default.Lightbulb, SuccessGreen) }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Recent Updates",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = BrandGreen
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (pendingRequests.isEmpty()) {
                item {
                    EmptyActivityView()
                }
            } else {
                items(pendingRequests) { requestWithStudent ->
                    DashboardStudyRequestItem(
                        requestWithStudent = requestWithStudent,
                        onAccept = { viewModel.acceptRequest(requestWithStudent) },
                        onReject = { viewModel.rejectRequest(requestWithStudent.request.requestId) }
                    )
                }
            }
            
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun TopNavButton(label: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(22.dp))
        }
        Text(text = label, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
fun LiveStatusCard(currentStatus: String, onStatusChange: (String) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(if(currentStatus == "Available") SuccessGreen else ErrorRed))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = "Current Status: $currentStatus", fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.weight(1f))
            Switch(
                checked = currentStatus == "Available",
                onCheckedChange = { onStatusChange(if(it) "Available" else "Busy") },
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = SuccessGreen)
            )
        }
    }
}

@Composable
fun ModernStatCard(label: String, value: String, icon: ImageVector, accentColor: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = value, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
            Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun TipCard(title: String, desc: String, icon: ImageVector, color: Color) {
    Surface(
        modifier = Modifier
            .width(260.dp)
            .height(140.dp),
        shape = RoundedCornerShape(24.dp),
        color = color,
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = desc, color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp, lineHeight = 18.sp)
        }
    }
}

@Composable
fun RecommendedPartnerCard(match: com.example.studypartnerapp.viewmodels.PartnerMatch, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .width(200.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            StudentAvatar(name = match.student.name, size = 48.dp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = match.student.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
            Text(text = match.student.course.take(20) + "...", fontSize = 10.sp, color = Color.Gray, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "${match.score}% Match",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun EmptyActivityView() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Inbox, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text("Your feed is clear", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Study requests will appear here",
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun DashboardNotificationItem(message: String, onDelete: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.1f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(BrandDeepGreen.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = BrandDeepGreen, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = message, 
                fontSize = 14.sp, 
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Close, contentDescription = "Dismiss", modifier = Modifier.size(18.dp), tint = Color.Gray)
            }
        }
    }
}

@Composable
fun DashboardStudyRequestItem(
    requestWithStudent: RequestWithStudent,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StudentAvatar(name = requestWithStudent.student.name, size = 44.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = requestWithStudent.student.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Wants to study ${requestWithStudent.student.course}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandGreen,
                        contentColor = Color.White
                    )
                ) {
                    Text("Accept", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 12.sp)
                }
                Button(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red,
                        contentColor = Color.White
                    )
                ) {
                    Text("Decline", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun UnitResourceCard(unitName: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .width(220.dp)
            .height(120.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF4F46E5), // Professional Vibrant Indigo
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Resources",
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = unitName,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}
