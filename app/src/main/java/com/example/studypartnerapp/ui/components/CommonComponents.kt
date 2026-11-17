package com.example.studypartnerapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.studypartnerapp.data.models.*
import com.example.studypartnerapp.navigation.Screen
import com.example.studypartnerapp.ui.theme.*
import com.example.studypartnerapp.viewmodels.ChatListViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AppBottomNavigation(
    navController: NavController,
    chatListViewModel: ChatListViewModel = viewModel()
) {
    val unreadCount by chatListViewModel.totalUnread.collectAsState()

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        val currentRoute = navController.currentDestination?.route
        NavigationBarItem(
            icon = { Icon(Icons.Default.GridView, contentDescription = null) },
            label = { Text("Hub", fontWeight = FontWeight.Bold) },
            selected = currentRoute == Screen.Dashboard.route,
            onClick = { 
                if (currentRoute != Screen.Dashboard.route) {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Search, contentDescription = null) },
            label = { Text("Find", fontWeight = FontWeight.Bold) },
            selected = currentRoute == Screen.FindPartners.route,
            onClick = { 
                if (currentRoute != Screen.FindPartners.route) {
                    navController.navigate(Screen.FindPartners.route)
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Diversity3, contentDescription = null) },
            label = { Text("Matches", fontWeight = FontWeight.Bold) },
            selected = currentRoute == Screen.Matches.route,
            onClick = { 
                if (currentRoute != Screen.Matches.route) {
                    navController.navigate(Screen.Matches.route)
                }
            }
        )
        NavigationBarItem(
            icon = {
                BadgedBox(
                    badge = {
                        if (unreadCount > 0) {
                            Badge(containerColor = SuccessGreen) {
                                Text(unreadCount.toString(), color = Color.White)
                            }
                        }
                    }
                ) {
                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null)
                }
            },
            label = { Text("Chats", fontWeight = FontWeight.Bold) },
            selected = currentRoute == Screen.ChatList.route,
            onClick = { 
                if (currentRoute != Screen.ChatList.route) {
                    navController.navigate(Screen.ChatList.route)
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = null) },
            label = { Text("Profile", fontWeight = FontWeight.Bold) },
            selected = currentRoute == Screen.Profile.route,
            onClick = { 
                if (currentRoute != Screen.Profile.route) {
                    navController.navigate(Screen.Profile.route)
                }
            }
        )
    }
}

@Composable
fun ExpertTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    keyboardType: androidx.compose.ui.text.input.KeyboardType = androidx.compose.ui.text.input.KeyboardType.Text,
    isPassword: Boolean = false
) {
    val isDarkBackground = isSystemInDarkTheme() || (MaterialTheme.colorScheme.background == BrandGreen)
    val textColor = if (isDarkBackground) Color.White else TextPrimary
    val placeholderColor = if (isDarkBackground) Color.White.copy(alpha = 0.6f) else TextSecondary
    val borderColor = if (isDarkBackground) Color.White.copy(alpha = 0.4f) else Color(0xFFCBD5E1)

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontWeight = FontWeight.ExtraBold, color = textColor) },
        placeholder = { 
            Text(
                text = "Enter $label", 
                color = placeholderColor, 
                fontWeight = FontWeight.Bold 
            ) 
        },
        modifier = modifier.fillMaxWidth().padding(vertical = 8.dp),
        leadingIcon = { Icon(icon, contentDescription = null, tint = BrandGold) },
        shape = RoundedCornerShape(12.dp),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = textColor,
            unfocusedTextColor = textColor,
            focusedPlaceholderColor = placeholderColor,
            unfocusedPlaceholderColor = placeholderColor,
            cursorColor = BrandGold,
            focusedLabelColor = BrandGold,
            unfocusedLabelColor = textColor,
            focusedBorderColor = BrandGold,
            unfocusedBorderColor = borderColor,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent
        ),
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        textStyle = LocalTextStyle.current.copy(fontWeight = FontWeight.ExtraBold, color = textColor)
    )
}

@Composable
fun StudentAvatar(
    name: String,
    size: Dp = 50.dp,
    fontSize: Int = 20,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(BrandGreen),
        contentAlignment = Alignment.Center
    ) {
        val initial = if (name.isNotEmpty()) name.take(1).uppercase() else "?"
        Text(
            text = initial,
            color = Color.White,
            fontSize = fontSize.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDropdownMenu(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text(label, fontWeight = FontWeight.ExtraBold, color = BrandGreen) },
            placeholder = { Text("Select $label", color = TextSecondary, fontWeight = FontWeight.Bold) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedPlaceholderColor = TextSecondary,
                unfocusedPlaceholderColor = TextSecondary,
                focusedLabelColor = BrandGreen,
                unfocusedLabelColor = BrandGreen,
                focusedBorderColor = BrandGreen,
                unfocusedBorderColor = Color(0xFFCBD5E1),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            textStyle = LocalTextStyle.current.copy(fontWeight = FontWeight.Bold)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, fontWeight = FontWeight.Bold) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    containerColor: Color = BrandGreen
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        enabled = enabled && !isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = Color.White,
            disabledContainerColor = containerColor.copy(alpha = 0.5f),
            disabledContentColor = Color.White.copy(alpha = 0.5f)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AvailabilityPicker(
    selectedOptions: List<String>,
    onOptionToggled: (String) -> Unit
) {
    val options = listOf("Morning", "Afternoon", "Evening", "Weekend")

    Column {
        Text(
            text = "Availability",
            fontWeight = FontWeight.ExtraBold,
            color = BrandGreen,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                val isSelected = selectedOptions.contains(option)
                FilterChip(
                    selected = isSelected,
                    onClick = { onOptionToggled(option) },
                    label = { Text(option, fontWeight = FontWeight.Bold) },
                    leadingIcon = if (isSelected) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null,
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BrandGreen,
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White,
                        labelColor = TextSecondary
                    )
                )
            }
        }
    }
}

@Composable
fun EmptyStateView(
    message: String,
    description: String = "",
    icon: ImageVector? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = Color.LightGray
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        Text(
            text = message,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        if (description.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ProfileCompletionIndicator(progress: Float) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Profile Completion", fontWeight = FontWeight.Bold)
            Text(text = "${(progress * 100).toInt()}%", fontWeight = FontWeight.Bold, color = BrandGreen)
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
            color = BrandGreen,
            trackColor = Color.LightGray.copy(alpha = 0.3f)
        )
    }
}

@Composable
fun PortalCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        shadowElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun StudyRequestItem(
    requestWithStudent: RequestWithStudent,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    PortalCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            StudentAvatar(name = requestWithStudent.student.name, size = 50.dp)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = requestWithStudent.student.name,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandGreen
                )
                Text(
                    text = "Wants to study ${requestWithStudent.student.course}",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onAccept,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SuccessGreen,
                    contentColor = Color.White
                )
            ) {
                Text("Accept", fontWeight = FontWeight.Bold, color = Color.White)
            }
            OutlinedButton(
                onClick = onReject,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed)
            ) {
                Text("Decline", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun UnitChip(
    unitName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        color = BrandDeepGreen.copy(alpha = 0.05f),
        border = androidx.compose.foundation.BorderStroke(1.dp, BrandDeepGreen.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.MenuBook,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = BrandDeepGreen
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = unitName,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = BrandDeepGreen
            )
        }
    }
}
