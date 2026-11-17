package com.example.studypartnerapp.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.studypartnerapp.ui.components.EmptyStateView
import com.example.studypartnerapp.ui.components.PortalCard
import com.example.studypartnerapp.ui.theme.*

/**
 * Screen to display learning resources for a specific academic unit.
 * Part of the "Unit Learning Resources" feature.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitResourcesScreen(
    unitName: String,
    navController: NavController
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Clean up unit name for URLs (replace spaces with +)
    val queryName = unitName.replace(" ", "+")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = unitName.ifEmpty { "Unit Resources" },
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "Explore learning resources for this unit",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandDeepGreen)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (unitName.isBlank()) {
            EmptyStateView(
                message = "Invalid Unit",
                description = "No unit name was provided. Please go back and try again.",
                icon = Icons.Default.ErrorOutline
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(scrollState)
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // 1. Books Card
                ResourceCard(
                    title = "Books & Textbooks",
                    description = "Search textbooks and reference materials on Open Library.",
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    buttonText = "Open Books",
                    accentColor = Color(0xFF6366F1), // Vibrant Indigo
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://openlibrary.org/search?q=$queryName"))
                        context.startActivity(intent)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Video Tutorials Card
                ResourceCard(
                    title = "Video Tutorials",
                    description = "Watch video lessons and tutorials on YouTube.",
                    icon = Icons.Default.PlayCircle,
                    buttonText = "Watch Videos",
                    accentColor = Color(0xFFFF0000), // YouTube Red
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=$queryName"))
                        context.startActivity(intent)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 3. Online Tutorials Card
                ResourceCard(
                    title = "Online Tutorials",
                    description = "Access specialized guides on GeeksforGeeks.",
                    icon = Icons.Default.Language,
                    buttonText = "Open Tutorials",
                    accentColor = SuccessGreen,
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=$queryName+GeeksforGeeks"))
                        context.startActivity(intent)
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Optional Enhancement: Recommended Platforms
                Text(
                    text = "Recommended Platforms",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = BrandGreen,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AssistChip(
                        onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://openlibrary.org"))) },
                        label = { Text("Open Library") },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.LibraryBooks, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    AssistChip(
                        onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://youtube.com"))) },
                        label = { Text("YouTube") },
                        leadingIcon = { Icon(Icons.Default.VideoLibrary, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                }
                
                AssistChip(
                    onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://geeksforgeeks.org"))) },
                    label = { Text("GeeksforGeeks") },
                    leadingIcon = { Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun ResourceCard(
    title: String,
    description: String,
    icon: ImageVector,
    buttonText: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    PortalCard {
        Row(verticalAlignment = Alignment.Top) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = accentColor.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = accentColor)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = description, fontSize = 13.sp, color = Color.Gray, lineHeight = 18.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = Color.White),
                    modifier = Modifier.height(40.dp)
                ) {
                    Text(buttonText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}
