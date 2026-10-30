package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.player.VantaPlayerScreen
import com.example.ui.ai.CineAiScreen
import com.example.ui.detail.MediaDetailScreen
import com.example.ui.home.HomeScreen
import com.example.ui.importing.ImportContentScreen
import com.example.ui.library.LibraryScreen
import com.example.ui.search.SearchScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.VantaAccentCyan
import com.example.ui.theme.VantaBackground
import com.example.ui.theme.VantaOutline
import com.example.ui.theme.VantaPrimary
import com.example.ui.theme.VantaSurface
import com.example.ui.theme.VantaTextPrimary
import com.example.ui.theme.VantaTextSecondary
import com.example.ui.theme.VantaTheme
import com.example.ui.viewmodel.VantaScreen
import com.example.ui.viewmodel.VantaViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: VantaViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            VantaTheme {
                val currentScreen by viewModel.currentScreen.collectAsState()
                val allMedia by viewModel.allMedia.collectAsState()
                val continueWatching by viewModel.continueWatching.collectAsState()
                val recentlyAdded by viewModel.recentlyAdded.collectAsState()

                val isPlayerActive = currentScreen is VantaScreen.Player
                val showBottomNav = currentScreen in listOf(
                    VantaScreen.Home,
                    VantaScreen.Library,
                    VantaScreen.Search,
                    VantaScreen.CineAI
                )

                BackHandler(enabled = currentScreen != VantaScreen.Home) {
                    viewModel.navigateBack()
                }

                Scaffold(
                    bottomBar = {
                        if (showBottomNav) {
                            VantaBottomNavigationBar(
                                currentScreen = currentScreen,
                                onSelectScreen = { viewModel.navigateTo(it) }
                            )
                        }
                    },
                    containerColor = VantaBackground,
                    contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = if (showBottomNav) 80.dp else 0.dp)
                    ) {
                        Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
                            when (screen) {
                                is VantaScreen.Home -> {
                                    HomeScreen(
                                        allMedia = allMedia,
                                        continueWatching = continueWatching,
                                        recentlyAdded = recentlyAdded,
                                        onSelectMedia = { viewModel.navigateTo(VantaScreen.Detail(it.id)) },
                                        onPlayMedia = { media ->
                                            if (media.videoUri != null) {
                                                viewModel.navigateTo(
                                                    VantaScreen.Player(
                                                        videoUri = media.videoUri,
                                                        title = media.title,
                                                        subtitle = media.releaseYear.toString(),
                                                        positionMs = media.playbackPositionMs,
                                                        mediaId = media.id
                                                    )
                                                )
                                            } else {
                                                viewModel.navigateTo(VantaScreen.Detail(media.id))
                                            }
                                        },
                                        onNavigateToImport = { viewModel.navigateTo(VantaScreen.Import) },
                                        onNavigateToSearch = { viewModel.navigateTo(VantaScreen.Search) },
                                        onNavigateToSettings = { viewModel.navigateTo(VantaScreen.Settings) },
                                        onNavigateToCineAI = { viewModel.navigateTo(VantaScreen.CineAI) },
                                        onLoadDemoContent = { viewModel.loadDemoShowcase() }
                                    )
                                }

                                is VantaScreen.Library -> {
                                    LibraryScreen(
                                        mediaList = allMedia,
                                        onSelectMedia = { viewModel.navigateTo(VantaScreen.Detail(it.id)) },
                                        onNavigateToImport = { viewModel.navigateTo(VantaScreen.Import) }
                                    )
                                }

                                is VantaScreen.Search -> {
                                    SearchScreen(
                                        allMedia = allMedia,
                                        onSelectMedia = { viewModel.navigateTo(VantaScreen.Detail(it.id)) },
                                        onNavigateBack = { viewModel.navigateBack() }
                                    )
                                }

                                is VantaScreen.CineAI -> {
                                    CineAiScreen(
                                        onNavigateBack = { viewModel.navigateBack() }
                                    )
                                }

                                is VantaScreen.Settings -> {
                                    SettingsScreen(
                                        mediaList = allMedia,
                                        onLoadDemo = { viewModel.loadDemoShowcase() },
                                        onClearDemo = { viewModel.clearDemoShowcase() },
                                        onNavigateBack = { viewModel.navigateBack() }
                                    )
                                }

                                is VantaScreen.Import -> {
                                    ImportContentScreen(
                                        onSaveMedia = { media, seasons, episodes ->
                                            viewModel.saveMediaWithContent(media, seasons, episodes)
                                        },
                                        onNavigateBack = { viewModel.navigateBack() }
                                    )
                                }

                                is VantaScreen.Detail -> {
                                    val mediaFlow = viewModel.getMediaById(screen.mediaId)
                                    val mediaItem by mediaFlow.collectAsState(initial = null)
                                    val seasons by viewModel.getSeasonsForSeries(screen.mediaId).collectAsState(initial = emptyList())
                                    val episodes by viewModel.getEpisodesForSeries(screen.mediaId).collectAsState(initial = emptyList())

                                    if (mediaItem != null) {
                                        MediaDetailScreen(
                                            media = mediaItem!!,
                                            seasons = seasons,
                                            episodes = episodes,
                                            onPlayMedia = { videoUri, title, subtitle, pos ->
                                                val epMatch = episodes.firstOrNull { it.videoUri == videoUri }
                                                viewModel.navigateTo(
                                                    VantaScreen.Player(
                                                        videoUri = videoUri,
                                                        title = title,
                                                        subtitle = subtitle,
                                                        positionMs = pos,
                                                        mediaId = mediaItem!!.id,
                                                        episodeId = epMatch?.id
                                                    )
                                                )
                                            },
                                            onToggleFavorite = { fav ->
                                                viewModel.toggleFavorite(mediaItem!!.id, fav)
                                            },
                                            onDeleteMedia = {
                                                viewModel.deleteMedia(mediaItem!!.id)
                                            },
                                            onAddEpisode = { ep ->
                                                viewModel.addEpisode(ep)
                                            },
                                            onDeleteEpisode = { epId ->
                                                viewModel.deleteEpisode(epId)
                                            },
                                            onUpdateMedia = { updated ->
                                                viewModel.updateMedia(updated)
                                            },
                                            onNavigateBack = { viewModel.navigateBack() }
                                        )
                                    }
                                }

                                is VantaScreen.Player -> {
                                    VantaPlayerScreen(
                                        videoUriString = screen.videoUri,
                                        title = screen.title,
                                        subTitle = screen.subtitle,
                                        initialPositionMs = screen.positionMs,
                                        onProgressUpdate = { pos, dur, completed ->
                                            viewModel.updatePlaybackProgress(
                                                mediaId = screen.mediaId,
                                                episodeId = screen.episodeId,
                                                positionMs = pos,
                                                durationMs = dur,
                                                isCompleted = completed
                                            )
                                        },
                                        onNavigateBack = { viewModel.navigateBack() }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VantaBottomNavigationBar(
    currentScreen: VantaScreen,
    onSelectScreen: (VantaScreen) -> Unit
) {
    NavigationBar(
        containerColor = VantaSurface,
        contentColor = VantaTextPrimary,
        tonalElevation = 8.dp,
        modifier = Modifier.testTag("bottom_nav_bar")
    ) {
        val items = listOf(
            Triple(VantaScreen.Home, "Home", Pair(Icons.Filled.Home, Icons.Outlined.Home)),
            Triple(VantaScreen.Library, "Library", Pair(Icons.Filled.VideoLibrary, Icons.Outlined.VideoLibrary)),
            Triple(VantaScreen.Search, "Search", Pair(Icons.Filled.Search, Icons.Outlined.Search)),
            Triple(VantaScreen.CineAI, "CineAI", Pair(Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome))
        )

        items.forEach { (screen, label, icons) ->
            val isSelected = currentScreen == screen
            NavigationBarItem(
                selected = isSelected,
                onClick = { onSelectScreen(screen) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) icons.first else icons.second,
                        contentDescription = label,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = if (screen == VantaScreen.CineAI) VantaAccentCyan else VantaPrimary,
                    selectedTextColor = VantaTextPrimary,
                    indicatorColor = (if (screen == VantaScreen.CineAI) VantaAccentCyan else VantaPrimary).copy(alpha = 0.15f),
                    unselectedIconColor = VantaTextSecondary,
                    unselectedTextColor = VantaTextSecondary
                ),
                modifier = Modifier.testTag("nav_item_${label.lowercase()}")
            )
        }
    }
}
