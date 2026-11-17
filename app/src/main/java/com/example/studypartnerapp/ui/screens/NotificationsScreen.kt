package com.example.studypartnerapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.studypartnerapp.data.models.Notification
import com.example.studypartnerapp.navigation.Screen
import com.example.studypartnerapp.ui.components.AppBottomNavigation
import com.example.studypartnerapp.ui.components.EmptyStateView
import com.example.studypartnerapp.ui.theme.*
import com.example.studypartnerapp.utils.Resource
import com.example.studypartnerapp.viewmodels.NotificationViewModel
import kotlinx.coroutines.flow.collect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    navController: NavController,
    viewModel: NotificationViewModel = viewModel()
) {
    val notificationsState by viewModel.notifications.collectAsState()

    LaunchedEffect(navController, viewModel) {
        navController.currentBackStackEntryFlow.collect { entry ->
            if (entry.destination.route == Screen.Notifications.route) {
                viewModel.loadData()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Alerts", fontWeight = FontWeight.ExtraBold, color = Color.White) },
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
            if (notificationsState is Resource.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                val notifications = (notificationsState as? Resource.Success<List<Notification>>)?.data ?: emptyList()

                if (notifications.isEmpty()) {
                    EmptyStateView(
                        message = "Your Inbox is Clear",
                        description = "No new alerts or study requests at this time.",
                        icon = Icons.Default.NotificationsNone
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                text = "Recent Alerts",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                        items(notifications) { notification ->
                            AlertNotificationItem(
                                message = notification.message,
                                onDelete = { viewModel.deleteNotification(notification.notificationId) },
                                onClick = if (notification.type == "study_request") {
                                    { navController.navigate(Screen.Dashboard.route) }
                                } else {
                                    null
                                },
                                dismissible = notification.type != "study_request"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AlertNotificationItem(
    message: String,
    onDelete: () -> Unit,
    onClick: (() -> Unit)? = null,
    dismissible: Boolean = true
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.1f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = message,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            if (dismissible) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Close, contentDescription = "Dismiss", modifier = Modifier.size(18.dp), tint = Color.Gray)
                }
            }
        }
    }
}
