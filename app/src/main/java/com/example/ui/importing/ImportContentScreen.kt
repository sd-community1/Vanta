package com.example.ui.importing

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.EpisodeEntity
import com.example.data.model.MediaEntity
import com.example.data.model.MediaType
import com.example.data.model.SeasonEntity
import com.example.ui.theme.VantaAccentCyan
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

data class PendingEpisode(
    val id: String = UUID.randomUUID().toString(),
    var seasonNumber: Int = 1,
    var episodeNumber: Int = 1,
    var title: String = "",
    var description: String = "",
    var videoUri: String = "",
    var thumbnailUri: String? = null,
    var durationMs: Long = 0L,
    var resolution: String = ""
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ImportContentScreen(
    onSaveMedia: (media: MediaEntity, seasons: List<SeasonEntity>, episodes: List<EpisodeEntity>) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    var selectedType by remember { mutableStateOf(MediaType.MOVIE) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var releaseYear by remember { mutableStateOf("2024") }
    var selectedGenre by remember { mutableStateOf("Action, Sci-Fi") }
    var posterUriString by remember { mutableStateOf<String?>(null) }

    // For Movie
    var movieVideoUriString by remember { mutableStateOf<String?>(null) }
    var movieDurationMs by remember { mutableLongStateOf(0L) }
    var movieResolution by remember { mutableStateOf("") }
    var movieFileName by remember { mutableStateOf("") }

    // For Series / Anime
    var seasonCount by remember { mutableIntStateOf(1) }
    val pendingEpisodes = remember { mutableStateListOf<PendingEpisode>() }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Pick Poster Image (Zero-permission photo picker)
    val posterPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            StorageAccessHelper.takePersistableUriPermission(context, it)
            posterUriString = it.toString()
        }
    }

    // Pick Movie Video File (SAF OpenDocument)
    val movieVideoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            StorageAccessHelper.takePersistableUriPermission(context, it)
            movieVideoUriString = it.toString()
            val meta = StorageAccessHelper.extractVideoMetadata(context, it)
            movieDurationMs = meta.durationMs
            movieResolution = meta.resolution
            val fn = StorageAccessHelper.getFileName(context, it)
            movieFileName = fn
            if (title.isBlank()) {
                title = fn
            }
        }
    }

    val genrePresets = listOf("Action", "Sci-Fi", "Anime", "Cyberpunk", "Drama", "Thriller", "Adventure", "Fantasy", "Mystery")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Import Local Media",
                        color = VantaTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("import_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = VantaTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VantaBackground)
            )
        },
        containerColor = VantaBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Type Selector Tab
            TabRow(
                selectedTabIndex = selectedType.ordinal,
                containerColor = VantaSurface,
                contentColor = VantaPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedType.ordinal]),
                        color = VantaPrimary
                    )
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .testTag("type_selector_tab")
            ) {
                MediaType.entries.forEach { type ->
                    Tab(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        text = {
                            Text(
                                text = when (type) {
                                    MediaType.MOVIE -> "Movie"
                                    MediaType.SERIES -> "TV Series"
                                    MediaType.ANIME -> "Anime"
                                },
                                fontWeight = if (selectedType == type) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedType == type) VantaTextPrimary else VantaTextSecondary
                            )
                        }
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Poster & Basic Details Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Poster Box
                Box(
                    modifier = Modifier
                        .width(110.dp)
                        .aspectRatio(2f / 3f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(VantaSurfaceElevated)
                        .border(1.dp, VantaOutline, RoundedCornerShape(10.dp))
                        .clickable {
                            posterPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                        .testTag("poster_picker_box"),
                    contentAlignment = Alignment.Center
                ) {
                    if (posterUriString != null) {
                        AsyncImage(
                            model = posterUriString,
                            contentDescription = "Poster Preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, tint = VantaTextSecondary, modifier = Modifier.size(32.dp))
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Select Poster",
                                color = VantaTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Title & Year
                Column(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        singleLine = true,
                        colors = outlinedColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_title")
                    )

                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = releaseYear,
                        onValueChange = { releaseYear = it },
                        label = { Text("Release Year") },
                        singleLine = true,
                        colors = outlinedColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_year")
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description / Synopsis") },
                minLines = 3,
                maxLines = 5,
                colors = outlinedColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_description")
            )

            Spacer(Modifier.height(16.dp))

            // Genre Selector
            Text(
                text = "Genres",
                color = VantaTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                genrePresets.forEach { g ->
                    val isSelected = selectedGenre.contains(g)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedGenre = if (isSelected) {
                                selectedGenre.split(", ").filter { it != g }.joinToString(", ")
                            } else {
                                if (selectedGenre.isBlank()) g else "$selectedGenre, $g"
                            }
                        },
                        label = { Text(g, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = VantaPrimary.copy(alpha = 0.2f),
                            selectedLabelColor = VantaPrimary,
                            containerColor = VantaSurface,
                            labelColor = VantaTextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (isSelected) VantaPrimary else VantaOutline,
                            enabled = true,
                            selected = isSelected
                        )
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = VantaOutline)
            Spacer(Modifier.height(20.dp))

            // Movie Video File Section
            if (selectedType == MediaType.MOVIE) {
                Text(
                    text = "Video File",
                    color = VantaTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = VantaSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (movieVideoUriString != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.VideoFile, contentDescription = null, tint = VantaPrimary, modifier = Modifier.size(32.dp))
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = movieFileName.ifBlank { "Selected Video" },
                                        color = VantaTextPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "${StorageAccessHelper.formatDurationReadable(movieDurationMs)} • $movieResolution",
                                        color = VantaTextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = { movieVideoPickerLauncher.launch(arrayOf("video/*")) },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = VantaTextPrimary),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Change Video File")
                            }
                        } else {
                            Text(
                                text = "Select an MP4, MKV, WebM, or other local video file from device storage.",
                                color = VantaTextSecondary,
                                fontSize = 13.sp
                            )
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = { movieVideoPickerLauncher.launch(arrayOf("video/*")) },
                                colors = ButtonDefaults.buttonColors(containerColor = VantaPrimary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("pick_movie_video_button")
                            ) {
                                Icon(Icons.Default.VideoFile, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Select Video File")
                            }
                        }
                    }
                }
            } else {
                // Series / Anime Seasons and Episodes Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Seasons & Episodes",
                        color = VantaTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedButton(
                        onClick = {
                            val nextEpNum = pendingEpisodes.size + 1
                            pendingEpisodes.add(
                                PendingEpisode(
                                    seasonNumber = 1,
                                    episodeNumber = nextEpNum,
                                    title = "Episode $nextEpNum"
                                )
                            )
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = VantaPrimary),
                        modifier = Modifier.testTag("add_episode_row_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Add Episode")
                    }
                }

                Spacer(Modifier.height(12.dp))

                if (pendingEpisodes.isEmpty()) {
                    Surface(
                        color = VantaSurface,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Tv, contentDescription = null, tint = VantaTextSecondary, modifier = Modifier.size(36.dp))
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "No episodes added yet.",
                                color = VantaTextSecondary,
                                fontSize = 13.sp
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Tap 'Add Episode' to link video files for this series.",
                                color = VantaTextSecondary.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                        }
                    }
                } else {
                    pendingEpisodes.forEachIndexed { index, ep ->
                        EpisodeImportCard(
                            episode = ep,
                            onUpdate = { updated ->
                                pendingEpisodes[index] = updated
                            },
                            onRemove = {
                                pendingEpisodes.removeAt(index)
                            }
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }

            // Error display if validation fails
            errorMessage?.let { err ->
                Spacer(Modifier.height(16.dp))
                Text(
                    text = err,
                    color = Color(0xFFFF5252),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(28.dp))

            // Save Button
            Button(
                onClick = {
                    if (title.isBlank()) {
                        errorMessage = "Please enter a title."
                        return@Button
                    }
                    if (selectedType == MediaType.MOVIE && movieVideoUriString.isNullOrBlank()) {
                        errorMessage = "Please select a local video file for the movie."
                        return@Button
                    }
                    if (selectedType != MediaType.MOVIE && pendingEpisodes.isEmpty()) {
                        errorMessage = "Please add at least one episode with a video file."
                        return@Button
                    }
                    if (selectedType != MediaType.MOVIE && pendingEpisodes.any { it.videoUri.isBlank() }) {
                        errorMessage = "Every episode must have a video file selected."
                        return@Button
                    }

                    val mediaId = UUID.randomUUID().toString()
                    val mediaEntity = MediaEntity(
                        id = mediaId,
                        type = selectedType.name,
                        title = title.trim(),
                        description = description.trim(),
                        releaseYear = releaseYear.toIntOrNull() ?: 2024,
                        genre = selectedGenre,
                        posterUri = posterUriString,
                        backdropUri = posterUriString,
                        videoUri = if (selectedType == MediaType.MOVIE) movieVideoUriString else null,
                        durationMs = if (selectedType == MediaType.MOVIE) movieDurationMs else 0L
                    )

                    val seasonsList = mutableListOf<SeasonEntity>()
                    val episodesList = mutableListOf<EpisodeEntity>()

                    if (selectedType != MediaType.MOVIE) {
                        val seasonsPresent = pendingEpisodes.map { it.seasonNumber }.distinct().sorted()
                        seasonsPresent.forEach { sNum ->
                            seasonsList.add(
                                SeasonEntity(
                                    id = UUID.randomUUID().toString(),
                                    seriesId = mediaId,
                                    seasonNumber = sNum,
                                    title = "Season $sNum"
                                )
                            )
                        }

                        pendingEpisodes.forEach { ep ->
                            episodesList.add(
                                EpisodeEntity(
                                    id = ep.id,
                                    seriesId = mediaId,
                                    seasonNumber = ep.seasonNumber,
                                    episodeNumber = ep.episodeNumber,
                                    title = ep.title.ifBlank { "Episode ${ep.episodeNumber}" },
                                    description = ep.description,
                                    thumbnailUri = ep.thumbnailUri ?: posterUriString,
                                    videoUri = ep.videoUri,
                                    durationMs = ep.durationMs
                                )
                            )
                        }
                    }

                    onSaveMedia(mediaEntity, seasonsList, episodesList)
                },
                colors = ButtonDefaults.buttonColors(containerColor = VantaPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_media_button")
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Save to VANTA Library",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun EpisodeImportCard(
    episode: PendingEpisode,
    onUpdate: (PendingEpisode) -> Unit,
    onRemove: () -> Unit
) {
    val context = LocalContext.current

    val epVideoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            StorageAccessHelper.takePersistableUriPermission(context, it)
            val meta = StorageAccessHelper.extractVideoMetadata(context, it)
            val fn = StorageAccessHelper.getFileName(context, it)
            onUpdate(
                episode.copy(
                    videoUri = it.toString(),
                    durationMs = meta.durationMs,
                    resolution = meta.resolution,
                    title = if (episode.title.startsWith("Episode ")) fn else episode.title
                )
            )
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = VantaSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "S${episode.seasonNumber} • E${episode.episodeNumber}",
                        color = VantaAccentCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = episode.title.ifBlank { "Untitled" },
                        color = VantaTextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }

                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFF5252), modifier = Modifier.size(18.dp))
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = episode.seasonNumber.toString(),
                    onValueChange = { onUpdate(episode.copy(seasonNumber = it.toIntOrNull() ?: 1)) },
                    label = { Text("Season") },
                    singleLine = true,
                    colors = outlinedColors(),
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = episode.episodeNumber.toString(),
                    onValueChange = { onUpdate(episode.copy(episodeNumber = it.toIntOrNull() ?: 1)) },
                    label = { Text("Episode #") },
                    singleLine = true,
                    colors = outlinedColors(),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = episode.title,
                onValueChange = { onUpdate(episode.copy(title = it)) },
                label = { Text("Episode Title") },
                singleLine = true,
                colors = outlinedColors(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            if (episode.videoUri.isNotBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(VantaSurfaceElevated)
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Linked: ${StorageAccessHelper.formatDurationReadable(episode.durationMs)} ${if (episode.resolution.isNotBlank()) "(${episode.resolution})" else ""}",
                        color = VantaTextPrimary,
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { epVideoPickerLauncher.launch(arrayOf("video/*")) }) {
                        Text("Change", fontSize = 11.sp, color = VantaPrimary)
                    }
                }
            } else {
                OutlinedButton(
                    onClick = { epVideoPickerLauncher.launch(arrayOf("video/*")) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = VantaPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.VideoFile, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Select Episode Video", fontSize = 12.sp)
                }
            }
        }
    }
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
