package com.example.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MediaEntity
import com.example.data.model.MediaType
import com.example.ui.theme.VantaAccentCyan
import com.example.ui.theme.VantaBackground
import com.example.ui.theme.VantaOutline
import com.example.ui.theme.VantaPrimary
import com.example.ui.theme.VantaSurface
import com.example.ui.theme.VantaSurfaceVariant
import com.example.ui.theme.VantaTextPrimary
import com.example.ui.theme.VantaTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    mediaList: List<MediaEntity>,
    onLoadDemo: () -> Unit,
    onClearDemo: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val moviesCount = mediaList.count { it.mediaType == MediaType.MOVIE }
    val seriesCount = mediaList.count { it.mediaType == MediaType.SERIES }
    val animeCount = mediaList.count { it.mediaType == MediaType.ANIME }
    val hasDemoContent = mediaList.any { it.id.startsWith("demo_") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VantaBackground)
            .testTag("settings_screen")
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Settings & Library",
                    color = VantaTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = VantaTextPrimary)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = VantaBackground)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            // Demonstration Showcase
            SectionTitle(title = "Demonstration Showcase")
            Card(
                colors = CardDefaults.cardColors(containerColor = VantaSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Demonstration Content",
                        color = VantaTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Load high-fidelity demonstration content to experience VANTA's video player, audio track selectors, gestures, and series UI immediately.",
                        color = VantaTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                    Spacer(Modifier.height(14.dp))

                    if (!hasDemoContent) {
                        Button(
                            onClick = onLoadDemo,
                            colors = ButtonDefaults.buttonColors(containerColor = VantaPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("load_demo_button")
                        ) {
                            Icon(Icons.Default.Movie, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Load Demonstration Showcase")
                        }
                    } else {
                        OutlinedButton(
                            onClick = onClearDemo,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("clear_demo_button")
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Remove Demonstration Content")
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Storage & Local Database
            SectionTitle(title = "Local Data & Offline Storage")
            Card(
                colors = CardDefaults.cardColors(containerColor = VantaSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingsRow(
                        icon = Icons.Default.SdStorage,
                        title = "Local Database",
                        subtitle = "Room vanta_streaming.db (Offline-First)"
                    )
                    HorizontalDivider(color = VantaOutline, modifier = Modifier.padding(vertical = 10.dp))
                    SettingsRow(
                        icon = Icons.Default.Security,
                        title = "Storage Permissions",
                        subtitle = "Android Storage Access Framework (SAF) URI Permissions"
                    )
                    HorizontalDivider(color = VantaOutline, modifier = Modifier.padding(vertical = 10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatPill(count = mediaList.size, label = "Total Titles")
                        StatPill(count = moviesCount, label = "Movies")
                        StatPill(count = seriesCount, label = "Series")
                        StatPill(count = animeCount, label = "Anime")
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Player Gestures Guide
            SectionTitle(title = "Player Gestures & Shortcuts")
            Card(
                colors = CardDefaults.cardColors(containerColor = VantaSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    GestureRow(shortcut = "Double Tap Left", description = "Rewind 10 seconds")
                    GestureRow(shortcut = "Double Tap Right", description = "Fast-forward 10 seconds")
                    GestureRow(shortcut = "Drag Left Side", description = "Adjust screen brightness")
                    GestureRow(shortcut = "Drag Right Side", description = "Adjust audio volume")
                    GestureRow(shortcut = "Lock Icon", description = "Prevent accidental touches during playback")
                }
            }

            Spacer(Modifier.height(24.dp))

            // About VANTA
            SectionTitle(title = "About VANTA")
            Card(
                colors = CardDefaults.cardColors(containerColor = VantaSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "VANTA Local Streaming Platform",
                        color = VantaTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Version 1.0.0 • Native Android Build",
                        color = VantaAccentCyan,
                        fontSize = 12.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "VANTA operates completely offline without user accounts, subscriptions, or remote streaming servers. Your video files remain strictly on your device storage.",
                        color = VantaTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }

            Spacer(Modifier.height(60.dp))
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        color = VantaTextPrimary,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun SettingsRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = VantaPrimary, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, color = VantaTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = VantaTextSecondary, fontSize = 11.sp)
        }
    }
}

@Composable
private fun GestureRow(shortcut: String, description: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = VantaSurfaceVariant
        ) {
            Text(
                text = shortcut,
                color = VantaTextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        Text(
            text = description,
            color = VantaTextSecondary,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun StatPill(count: Int, label: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = VantaSurfaceVariant,
        modifier = Modifier.padding(horizontal = 2.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = count.toString(),
                color = VantaPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                color = VantaTextSecondary,
                fontSize = 10.sp
            )
        }
    }
}
