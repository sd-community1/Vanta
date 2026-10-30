package com.example.ui.detail

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.EpisodeEntity
import com.example.data.model.MediaEntity
import com.example.data.model.MediaType
import com.example.data.model.SeasonEntity
import com.example.ui.theme.VantaAccentCyan
import com.example.ui.theme.VantaAccentGold
import com.example.ui.theme.VantaBackground
import com.example.ui.theme.VantaOutline
import com.example.ui.theme.VantaPrimary
import com.example.ui.theme.VantaSurface
import com.example.ui.theme.VantaSurfaceElevated
import com.example.ui.theme.VantaSurfaceVariant
import com.example.ui.theme.VantaTextPrimary
import com.example.ui.theme.VantaTextSecondary
import com.example.util.StorageAccessHelper
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaDetailScreen(
    media: MediaEntity,
    seasons: List<SeasonEntity>,
    episodes: List<EpisodeEntity>,
    onPlayMedia: (videoUri: String, title: String, subtitle: String, positionMs: Long) -> Unit,
    onToggleFavorite: (Boolean) -> Unit,
    onDeleteMedia: () -> Unit,
    onAddEpisode: (EpisodeEntity) -> Unit,
    onDeleteEpisode: (String) -> Unit,
    onUpdateMedia: (MediaEntity) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAddEpisodeDialog by remember { mutableStateOf(false) }
    var selectedSeasonIndex by remember { mutableIntStateOf(0) }

    var isFileAccessible by remember { mutableStateOf(true) }

    LaunchedEffect(media.videoUri) {
        if (media.mediaType == MediaType.MOVIE && media.videoUri != null) {
            isFileAccessible = StorageAccessHelper.isUriAccessible(context, media.videoUri)
        }
    }

    // Video re-linking for movies if file was moved
    val relinkLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            StorageAccessHelper.takePersistableUriPermission(context, it)
            val meta = StorageAccessHelper.extractVideoMetadata(context, it)
            onUpdateMedia(
                media.copy(
                    videoUri = it.toString(),
                    durationMs = if (meta.durationMs > 0) meta.durationMs else media.durationMs
                )
            )
            isFileAccessible = true
        }
    }

    val availableSeasons = if (seasons.isEmpty() && media.mediaType != MediaType.MOVIE) {
        listOf(SeasonEntity(id = "default_s1", seriesId = media.id, seasonNumber = 1, title = "Season 1"))
    } else seasons

    val currentSeason = availableSeasons.getOrNull(selectedSeasonIndex) ?: availableSeasons.firstOrNull()
    val seasonEpisodes = episodes.filter { it.seasonNumber == (currentSeason?.seasonNumber ?: 1) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VantaBackground)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("detail_scroll_container")
        ) {
            // Header Backdrop
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
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

                    // Cinematic Gradient Overlays
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Black.copy(alpha = 0.5f),
                                        Color.Transparent,
                                        VantaBackground
                                    )
                                )
                            )
                    )

                    // Navigation & Actions row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp, start = 16.dp, end = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                .size(42.dp)
                                .testTag("detail_back_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = VantaTextPrimary)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            IconButton(
                                onClick = { onToggleFavorite(!media.isFavorite) },
                                modifier = Modifier
                                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                    .size(42.dp)
                                    .testTag("favorite_button")
                            ) {
                                Icon(
                                    if (media.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Favorite",
                                    tint = if (media.isFavorite) VantaPrimary else VantaTextPrimary
                                )
                            }

                            IconButton(
                                onClick = { showDeleteDialog = true },
                                modifier = Modifier
                                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                    .size(42.dp)
                                    .testTag("delete_media_button")
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFF5252))
                            }
                        }
                    }
                }
            }

            // Title & Meta Info
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Text(
                        text = media.title,
                        color = VantaTextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 30.sp
                    )

                    Spacer(Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = VantaPrimary.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = media.type,
                                color = VantaPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Text(
                            text = media.releaseYear.toString(),
                            color = VantaTextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )

                        if (media.durationMs > 0) {
                            Text(
                                text = "•",
                                color = VantaTextSecondary,
                                fontSize = 13.sp
                            )
                            Text(
                                text = StorageAccessHelper.formatDurationReadable(media.durationMs),
                                color = VantaTextSecondary,
                                fontSize = 13.sp
                            )
                        }

                        if (media.rating > 0f) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = VantaAccentGold, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(3.dp))
                                Text(
                                    text = String.format("%.1f", media.rating),
                                    color = VantaAccentGold,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = media.genre,
                        color = VantaAccentCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(Modifier.height(14.dp))

                    if (media.description.isNotBlank()) {
                        Text(
                            text = media.description,
                            color = VantaTextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }

            // Movie Play Actions / File Accessibility Check
            if (media.mediaType == MediaType.MOVIE) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    ) {
                        if (!isFileAccessible) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = VantaSurfaceVariant),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFFF5252))
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = "File inaccessible at original location",
                                            color = Color(0xFFFF5252),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = "The local video file could not be read. Tap below to relocate or select the file again.",
                                        color = VantaTextSecondary,
                                        fontSize = 12.sp
                                    )
                                    Spacer(Modifier.height(10.dp))
                                    OutlinedButton(
                                        onClick = { relinkLauncher.launch(arrayOf("video/*")) },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = VantaPrimary)
                                    ) {
                                        Text("Relink Local Video File")
                                    }
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                        } else {
                            // Primary Play Button
                            val hasProgress = media.playbackPositionMs > 0 && !media.isCompleted
                            Button(
                                onClick = {
                                    media.videoUri?.let { uri ->
                                        onPlayMedia(uri, media.title, media.releaseYear.toString(), media.playbackPositionMs)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = VantaPrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("movie_play_button")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(24.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = if (hasProgress) {
                                        "Resume (${StorageAccessHelper.formatDuration(media.playbackPositionMs)})"
                                    } else "Play Movie",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                            }

                            if (hasProgress) {
                                Spacer(Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { media.progressFraction },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp)),
                                    color = VantaPrimary,
                                    trackColor = VantaOutline
                                )
                            }
                        }

                        Spacer(Modifier.height(24.dp))
                    }
                }
            } else {
                // Series / Anime Season Selector & Episodes
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Episodes",
                                color = VantaTextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )

                            OutlinedButton(
                                onClick = { showAddEpisodeDialog = true },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = VantaPrimary),
                                modifier = Modifier.testTag("detail_add_episode_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Add Episode", fontSize = 12.sp)
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        if (availableSeasons.size > 1) {
                            ScrollableTabRow(
                                selectedTabIndex = selectedSeasonIndex,
                                containerColor = VantaBackground,
                                contentColor = VantaPrimary,
                                edgePadding = 20.dp,
                                indicator = { tabPositions ->
                                    TabRowDefaults.SecondaryIndicator(
                                        Modifier.tabIndicatorOffset(tabPositions[selectedSeasonIndex]),
                                        color = VantaPrimary
                                    )
                                }
                            ) {
                                availableSeasons.forEachIndexed { idx, s ->
                                    Tab(
                                        selected = selectedSeasonIndex == idx,
                                        onClick = { selectedSeasonIndex = idx },
                                        text = {
                                            Text(
                                                text = s.title,
                                                fontWeight = if (selectedSeasonIndex == idx) FontWeight.Bold else FontWeight.Normal,
                                                color = if (selectedSeasonIndex == idx) VantaTextPrimary else VantaTextSecondary
                                            )
                                        }
                                    )
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                        }
                    }
                }

                if (seasonEpisodes.isEmpty()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = VantaSurface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No episodes in this season.",
                                    color = VantaTextSecondary,
                                    fontSize = 13.sp
                                )
                                Spacer(Modifier.height(8.dp))
                                Button(
                                    onClick = { showAddEpisodeDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = VantaPrimary)
                                ) {
                                    Text("Add Episode")
                                }
                            }
                        }
                    }
                } else {
                    items(seasonEpisodes) { ep ->
                        EpisodeRowItem(
                            episode = ep,
                            seriesTitle = media.title,
                            onPlay = {
                                onPlayMedia(ep.videoUri, ep.title, "${media.title} • S${ep.seasonNumber}E${ep.episodeNumber}", ep.playbackPositionMs)
                            },
                            onDelete = {
                                onDeleteEpisode(ep.id)
                            }
                        )
                        Spacer(Modifier.height(10.dp))
                    }
                }
            }

            item {
                Spacer(Modifier.height(40.dp))
            }
        }
    }

    // Confirmation Dialog for Deleting Media
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "Remove from VANTA?",
                    color = VantaTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "This will remove \"${media.title}\" and its metadata from your VANTA library.\n\nYour original video files on device storage will NOT be deleted or modified.",
                    color = VantaTextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteMedia()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                    modifier = Modifier.testTag("confirm_delete_button")
                ) {
                    Text("Remove", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = VantaTextPrimary)
                }
            },
            containerColor = VantaSurfaceVariant
        )
    }

    // Add Episode Dialog
    if (showAddEpisodeDialog) {
        AddEpisodeDialog(
            seriesId = media.id,
            defaultSeason = currentSeason?.seasonNumber ?: 1,
            nextEpisodeNumber = (episodes.maxOfOrNull { it.episodeNumber } ?: 0) + 1,
            onDismiss = { showAddEpisodeDialog = false },
            onConfirm = { newEp ->
                showAddEpisodeDialog = false
                onAddEpisode(newEp)
            }
        )
    }
}

@Composable
private fun EpisodeRowItem(
    episode: EpisodeEntity,
    seriesTitle: String,
    onPlay: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    var isAccessible by remember { mutableStateOf(true) }

    LaunchedEffect(episode.videoUri) {
        isAccessible = StorageAccessHelper.isUriAccessible(context, episode.videoUri)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = VantaSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clickable { if (isAccessible) onPlay() }
            .testTag("episode_card_${episode.episodeNumber}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Thumbnail with Play Button
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(VantaSurfaceElevated)
                ) {
                    if (episode.thumbnailUri != null) {
                        AsyncImage(
                            model = episode.thumbnailUri,
                            contentDescription = episode.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.35f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "Play Episode",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                // Episode Title & Description
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Episode ${episode.episodeNumber}: ${episode.title}",
                        color = VantaTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (episode.durationMs > 0) {
                            Text(
                                text = StorageAccessHelper.formatDurationReadable(episode.durationMs),
                                color = VantaTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                        if (!isAccessible) {
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "File Missing",
                                color = Color(0xFFFF5252),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Remove episode",
                        tint = VantaTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Watched Progress Bar
            if (episode.playbackPositionMs > 0 && !episode.isCompleted) {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { episode.progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(1.5.dp)),
                    color = VantaPrimary,
                    trackColor = VantaOutline
                )
            }
        }
    }
}

@Composable
private fun AddEpisodeDialog(
    seriesId: String,
    defaultSeason: Int,
    nextEpisodeNumber: Int,
    onDismiss: () -> Unit,
    onConfirm: (EpisodeEntity) -> Unit
) {
    val context = LocalContext.current
    var seasonNum by remember { mutableIntStateOf(defaultSeason) }
    var episodeNum by remember { mutableIntStateOf(nextEpisodeNumber) }
    var title by remember { mutableStateOf("Episode $nextEpisodeNumber") }
    var description by remember { mutableStateOf("") }
    var videoUriString by remember { mutableStateOf("") }
    var durationMs by remember { mutableStateOf(0L) }
    var thumbnailUriString by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            StorageAccessHelper.takePersistableUriPermission(context, it)
            videoUriString = it.toString()
            val meta = StorageAccessHelper.extractVideoMetadata(context, it)
            durationMs = meta.durationMs
            val fn = StorageAccessHelper.getFileName(context, it)
            if (title.startsWith("Episode ")) {
                title = fn
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add Episode", color = VantaTextPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = seasonNum.toString(),
                        onValueChange = { seasonNum = it.toIntOrNull() ?: 1 },
                        label = { Text("Season") },
                        singleLine = true,
                        colors = outlinedColors(),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = episodeNum.toString(),
                        onValueChange = { episodeNum = it.toIntOrNull() ?: 1 },
                        label = { Text("Episode #") },
                        singleLine = true,
                        colors = outlinedColors(),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    colors = outlinedColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                if (videoUriString.isNotBlank()) {
                    Text(
                        text = "Video Linked (${StorageAccessHelper.formatDurationReadable(durationMs)})",
                        color = Color(0xFF00E676),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Button(
                        onClick = { videoPickerLauncher.launch(arrayOf("video/*")) },
                        colors = ButtonDefaults.buttonColors(containerColor = VantaPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.VideoFile, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Pick Video File")
                    }
                }

                errorMessage?.let {
                    Text(it, color = Color(0xFFFF5252), fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (videoUriString.isBlank()) {
                        errorMessage = "Please select a video file."
                        return@Button
                    }
                    onConfirm(
                        EpisodeEntity(
                            id = UUID.randomUUID().toString(),
                            seriesId = seriesId,
                            seasonNumber = seasonNum,
                            episodeNumber = episodeNum,
                            title = title.ifBlank { "Episode $episodeNum" },
                            description = description,
                            thumbnailUri = thumbnailUriString,
                            videoUri = videoUriString,
                            durationMs = durationMs
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = VantaPrimary)
            ) {
                Text("Add Episode")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = VantaTextPrimary)
            }
        },
        containerColor = VantaSurfaceVariant
    )
}

@Composable
private fun outlinedColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = VantaPrimary,
    unfocusedBorderColor = VantaOutline,
    focusedTextColor = VantaTextPrimary,
    unfocusedTextColor = VantaTextPrimary,
    focusedLabelColor = VantaPrimary,
    unfocusedLabelColor = VantaTextSecondary,
    cursorColor = VantaPrimary
)
