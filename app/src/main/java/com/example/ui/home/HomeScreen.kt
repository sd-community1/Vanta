package com.example.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.MediaEntity
import com.example.data.model.MediaType
import com.example.ui.components.ContinueWatchingCard
import com.example.ui.components.MediaPosterCard
import com.example.ui.theme.VantaAccentCyan
import com.example.ui.theme.VantaBackground
import com.example.ui.theme.VantaOutline
import com.example.ui.theme.VantaPrimary
import com.example.ui.theme.VantaSurface
import com.example.ui.theme.VantaSurfaceElevated
import com.example.ui.theme.VantaSurfaceVariant
import com.example.ui.theme.VantaTextPrimary
import com.example.ui.theme.VantaTextSecondary
import com.example.ui.theme.VantaTextTertiary

@Composable
fun HomeScreen(
    allMedia: List<MediaEntity>,
    continueWatching: List<MediaEntity>,
    recentlyAdded: List<MediaEntity>,
    onSelectMedia: (MediaEntity) -> Unit,
    onPlayMedia: (MediaEntity) -> Unit,
    onNavigateToImport: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToCineAI: () -> Unit,
    onLoadDemoContent: () -> Unit
) {
    val movies = allMedia.filter { it.mediaType == MediaType.MOVIE }
    val series = allMedia.filter { it.mediaType == MediaType.SERIES }
    val anime = allMedia.filter { it.mediaType == MediaType.ANIME }

    val featuredItem = continueWatching.firstOrNull() ?: allMedia.firstOrNull()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VantaBackground)
    ) {
        if (allMedia.isEmpty()) {
            EmptyLibraryOnboarding(
                onImportClick = onNavigateToImport,
                onLoadDemo = onLoadDemoContent,
                onSearchClick = onNavigateToSearch,
                onSettingsClick = onNavigateToSettings,
                onCineAIClick = onNavigateToCineAI
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("home_feed_list")
            ) {
                // Top Navigation Bar
                item {
                    HomeTopBar(
                        onSearchClick = onNavigateToSearch,
                        onSettingsClick = onNavigateToSettings,
                        onCineAIClick = onNavigateToCineAI,
                        onImportClick = onNavigateToImport
                    )
                }

                // Hero Featured Section
                featuredItem?.let { hero ->
                    item {
                        FeaturedHeroSection(
                            media = hero,
                            onPlay = { onPlayMedia(hero) },
                            onDetails = { onSelectMedia(hero) }
                        )
                    }
                }

                // Continue Watching
                if (continueWatching.isNotEmpty()) {
                    item {
                        HomeSection(
                            title = "Continue Watching",
                            content = {
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 20.dp),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    items(continueWatching) { item ->
                                        ContinueWatchingCard(
                                            media = item,
                                            onClick = { onSelectMedia(item) }
                                        )
                                    }
                                }
                            }
                        )
                    }
                }

                // Recently Added
                if (recentlyAdded.isNotEmpty()) {
                    item {
                        HomeSection(
                            title = "Recently Added",
                            content = {
                                MediaRow(items = recentlyAdded, onSelect = onSelectMedia)
                            }
                        )
                    }
                }

                // Movies Section
                if (movies.isNotEmpty()) {
                    item {
                        HomeSection(
                            title = "Movies",
                            subtitle = "${movies.size} titles",
                            content = {
                                MediaRow(items = movies, onSelect = onSelectMedia)
                            }
                        )
                    }
                }

                // TV Shows Section
                if (series.isNotEmpty()) {
                    item {
                        HomeSection(
                            title = "TV Shows",
                            subtitle = "${series.size} series",
                            content = {
                                MediaRow(items = series, onSelect = onSelectMedia)
                            }
                        )
                    }
                }

                // Anime Section
                if (anime.isNotEmpty()) {
                    item {
                        HomeSection(
                            title = "Anime",
                            subtitle = "${anime.size} series",
                            content = {
                                MediaRow(items = anime, onSelect = onSelectMedia)
                            }
                        )
                    }
                }

                item {
                    Spacer(Modifier.height(100.dp))
                }
            }
        }
    }
}

@Composable
private fun HomeTopBar(
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onCineAIClick: () -> Unit,
    onImportClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 44.dp, start = 20.dp, end = 16.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Logo & Brand
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.testTag("vanta_logo_header")
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = VantaPrimary,
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "V",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    text = "VANTA",
                    color = VantaTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "LOCAL STREAMING",
                    color = VantaPrimary,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        // Action Icons
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            IconButton(
                onClick = onImportClick,
                modifier = Modifier
                    .background(VantaSurfaceElevated, CircleShape)
                    .size(38.dp)
                    .testTag("home_import_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Import Video", tint = VantaTextPrimary, modifier = Modifier.size(20.dp))
            }

            IconButton(
                onClick = onCineAIClick,
                modifier = Modifier
                    .background(VantaSurfaceElevated, CircleShape)
                    .size(38.dp)
                    .testTag("home_cineai_button")
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = "CineAI", tint = VantaAccentCyan, modifier = Modifier.size(18.dp))
            }

            IconButton(
                onClick = onSearchClick,
                modifier = Modifier
                    .background(VantaSurfaceElevated, CircleShape)
                    .size(38.dp)
                    .testTag("home_search_button")
            ) {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = VantaTextPrimary, modifier = Modifier.size(20.dp))
            }

            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .background(VantaSurfaceElevated, CircleShape)
                    .size(38.dp)
                    .testTag("home_settings_button")
            ) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = VantaTextSecondary, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun FeaturedHeroSection(
    media: MediaEntity,
    onPlay: () -> Unit,
    onDetails: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = VantaSurface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clickable(onClick = onDetails)
            .testTag("featured_hero_card")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
        ) {
            val backdropModel = media.backdropUri ?: media.posterUri
            if (backdropModel != null) {
                AsyncImage(
                    model = backdropModel,
                    contentDescription = media.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(VantaSurfaceElevated)
                )
            }

            // Dark gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.5f),
                                Color.Black.copy(alpha = 0.95f)
                            )
                        )
                    )
            )

            // Content
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = VantaPrimary
                    ) {
                        Text(
                            text = "FEATURED",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = media.genre,
                        color = VantaAccentCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(Modifier.height(6.dp))

                Text(
                    text = media.title,
                    color = VantaTextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )

                Spacer(Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onPlay,
                        colors = ButtonDefaults.buttonColors(containerColor = VantaPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (media.playbackPositionMs > 0) "Resume" else "Play",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    FilledTonalButton(
                        onClick = onDetails,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color.White.copy(alpha = 0.15f),
                            contentColor = VantaTextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Details", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeSection(
    title: String,
    subtitle: String? = null,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 22.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                color = VantaTextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = VantaTextSecondary,
                    fontSize = 12.sp
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        content()
    }
}

@Composable
private fun MediaRow(
    items: List<MediaEntity>,
    onSelect: (MediaEntity) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items) { item ->
            MediaPosterCard(media = item, onClick = { onSelect(item) })
        }
    }
}

@Composable
private fun EmptyLibraryOnboarding(
    onImportClick: () -> Unit,
    onLoadDemo: () -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onCineAIClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        HomeTopBar(
            onSearchClick = onSearchClick,
            onSettingsClick = onSettingsClick,
            onCineAIClick = onCineAIClick,
            onImportClick = onImportClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = VantaSurfaceElevated,
                modifier = Modifier
                    .size(90.dp)
                    .border(1.dp, VantaOutline, CircleShape)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.VideoLibrary,
                        contentDescription = null,
                        tint = VantaPrimary,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Your Offline Theater is Empty",
                color = VantaTextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = "Import video files from your device storage to build your movie, series, and anime collection.",
                color = VantaTextSecondary,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(28.dp))

            Button(
                onClick = onImportClick,
                colors = ButtonDefaults.buttonColors(containerColor = VantaPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("empty_state_import_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Import Your First Video",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
            }

            Spacer(Modifier.height(14.dp))

            OutlinedButton(
                onClick = onLoadDemo,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = VantaTextPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("empty_state_demo_button")
            ) {
                Icon(Icons.Default.Movie, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Load Showcase Demonstration", fontSize = 13.sp)
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Demonstration content can be removed anytime in Settings.",
                color = VantaTextTertiary,
                fontSize = 11.sp
            )
        }
    }
}
