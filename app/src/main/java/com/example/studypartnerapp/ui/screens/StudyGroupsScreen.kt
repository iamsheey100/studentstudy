package com.example.studypartnerapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.studypartnerapp.data.models.Student
import com.example.studypartnerapp.data.models.StudyGroup
import com.example.studypartnerapp.ui.components.*
import com.example.studypartnerapp.ui.theme.*
import com.example.studypartnerapp.utils.Resource
import com.example.studypartnerapp.viewmodels.GroupViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyGroupsScreen(
    navController: NavController,
    viewModel: GroupViewModel = viewModel()
) {
    val groupsState by viewModel.groups.collectAsState()
    val groupMembers by viewModel.groupMembers.collectAsState()
    val operationMessage by viewModel.operationMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedGroupForDetails by remember { mutableStateOf<StudyGroup?>(null) }

    LaunchedEffect(operationMessage) {
        operationMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearOperationMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Study Groups Registry", fontWeight = FontWeight.ExtraBold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Create", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandDeepGreen)
            )
        },
        bottomBar = { AppBottomNavigation(navController) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (groupsState) {
                is Resource.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = BrandGreen)
                is Resource.Error -> EmptyStateView(message = "Registry Error", description = "Could not load study groups.")
                is Resource.Success -> {
                    val groups = (groupsState as Resource.Success<List<StudyGroup>>).data ?: emptyList()
                    val scope = rememberCoroutineScope()
                    if (groups.isEmpty()) {
                        EmptyStateView(message = "No Active Groups", description = "Be the first to create a study group for your unit.", icon = Icons.Default.Groups)
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(groups) { group ->
                                GroupPortalCard(
                                    group = group,
                                    currentUserId = viewModel.currentUserId,
                                    navController = navController,
                                    onJoin = { viewModel.joinGroup(group.groupId) },
                                    onViewDetails = {
                                        selectedGroupForDetails = group
                                        viewModel.loadGroupMembers(group.members)
                                    },
                                    onChat = {
                                        scope.launch {
                                            val chatId = viewModel.initGroupChat(group)
                                            navController.navigate(com.example.studypartnerapp.navigation.Screen.Chat.createRoute(chatId, "group", group.groupName))
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
    
    if (showCreateDialog) {
        CreateGroupDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, unit, time ->
                viewModel.createGroup(name, unit, time)
                showCreateDialog = false
            }
        )
    }

    selectedGroupForDetails?.let { group ->
        GroupDetailsDialog(
            group = group,
            members = groupMembers,
            onDismiss = { selectedGroupForDetails = null }
        )
    }
}

@Composable
fun GroupPortalCard(
    group: StudyGroup, 
    currentUserId: String?,
    navController: NavController,
    onJoin: () -> Unit, 
    onViewDetails: () -> Unit,
    onChat: () -> Unit
) {
    val isMember = currentUserId != null && group.members.contains(currentUserId)
    PortalCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(50.dp), shape = RoundedCornerShape(12.dp), color = BrandGreen.copy(alpha = 0.1f)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Diversity3, contentDescription = null, tint = BrandGreen, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = group.groupName, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = BrandGreen)
                Text(text = "Unit: ${group.unit}", fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Text(text = "Time: ${group.meetingTime}", fontSize = 12.sp, color = Color.Gray)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp), tint = BrandGreen)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "${group.members.size} Members Joined", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandGreen)
            Spacer(modifier = Modifier.weight(1f))
            TextButton(onClick = onViewDetails) {
                Text("View Members", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandGreen)
            }
            Spacer(modifier = Modifier.width(8.dp))
            
            if (isMember) {
                Button(
                    onClick = onChat,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandDeepGreen, contentColor = Color.White),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Chat Group", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = onJoin,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreen, contentColor = Color.White),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Join", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun GroupDetailsDialog(group: StudyGroup, members: List<Student>, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(group.groupName, fontWeight = FontWeight.Bold, color = BrandGreen) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Members in this group:", fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
                LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                    items(members) { student ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            StudentAvatar(name = student.name, size = 32.dp, fontSize = 14)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(student.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(student.course, fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close", fontWeight = FontWeight.Bold, color = BrandGreen) }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun CreateGroupDialog(onDismiss: () -> Unit, onCreate: (String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Study Group", fontWeight = FontWeight.Bold, color = BrandGreen) },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Group Name") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("Academic Unit") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = time, onValueChange = { time = it }, label = { Text("Meeting Time (e.g. 2 PM)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(name, unit, time) },
                enabled = name.isNotBlank() && unit.isNotBlank() && time.isNotBlank(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandGreen, contentColor = Color.White)
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
