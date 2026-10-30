package com.example.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.MediaEntity
import com.example.data.model.MediaType
import com.example.ui.components.MediaPosterCard
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

enum class LibraryFilter(val label: String) {
    ALL("All Content"),
    MOVIES("Movies"),
    SERIES("TV Series"),
    ANIME("Anime"),
    FAVORITES("Favorites"),
    CONTINUE_WATCHING("Continue Watching")
}

enum class SortOption(val label: String) {
    RECENTLY_ADDED("Recently Added"),
    TITLE("Title (A-Z)"),
    YEAR("Release Year"),
    RATING("Rating")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    mediaList: List<MediaEntity>,
    onSelectMedia: (MediaEntity) -> Unit,
    onNavigateToImport: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf(LibraryFilter.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedSort by remember { mutableStateOf(SortOption.RECENTLY_ADDED) }
    var isGridView by remember { mutableStateOf(true) }
    var showSortMenu by remember { mutableStateOf(false) }

    // Filter items
    val filteredList = mediaList.filter { item ->
        val matchesCategory = when (selectedFilter) {
            LibraryFilter.ALL -> true
            LibraryFilter.MOVIES -> item.mediaType == MediaType.MOVIE
            LibraryFilter.SERIES -> item.mediaType == MediaType.SERIES
            LibraryFilter.ANIME -> item.mediaType == MediaType.ANIME
            LibraryFilter.FAVORITES -> item.isFavorite
            LibraryFilter.CONTINUE_WATCHING -> item.playbackPositionMs > 0 && !item.isCompleted
        }

        val matchesSearch = searchQuery.isBlank() ||
                item.title.contains(searchQuery, ignoreCase = true) ||
                item.genre.contains(searchQuery, ignoreCase = true) ||
                item.description.contains(searchQuery, ignoreCase = true)

        matchesCategory && matchesSearch
    }.let { list ->
        when (selectedSort) {
            SortOption.RECENTLY_ADDED -> list.sortedByDescending { it.addedDate }
            SortOption.TITLE -> list.sortedBy { it.title.lowercase() }
            SortOption.YEAR -> list.sortedByDescending { it.releaseYear }
            SortOption.RATING -> list.sortedByDescending { it.rating }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VantaBackground)
            .testTag("library_screen")
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 44.dp, start = 20.dp, end = 20.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Library",
                    color = VantaTextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${filteredList.size} items stored",
                    color = VantaTextSecondary,
                    fontSize = 12.sp
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Layout Toggle
                IconButton(
                    onClick = { isGridView = !isGridView },
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                        contentDescription = "Toggle View",
                        tint = VantaTextPrimary
                    )
                }

                // Sort Button
                Box {
                    IconButton(
                        onClick = { showSortMenu = true },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(Icons.Default.Sort, contentDescription = "Sort", tint = VantaTextPrimary)
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false },
                        containerColor = VantaSurfaceVariant
                    ) {
                        SortOption.entries.forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = option.label,
                                        color = if (selectedSort == option) VantaPrimary else VantaTextPrimary,
                                        fontWeight = if (selectedSort == option) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    selectedSort = option
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }

                // Add button
                IconButton(
                    onClick = onNavigateToImport,
                    modifier = Modifier
                        .background(VantaPrimary, RoundedCornerShape(8.dp))
                        .size(38.dp)
                        .testTag("library_add_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Import Media", tint = Color.White)
                }
            }
        }

        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Filter your titles, genres, series...", fontSize = 13.sp) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = VantaTextSecondary, modifier = Modifier.size(18.dp))
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = VantaTextSecondary)
                    }
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = VantaPrimary,
                unfocusedBorderColor = VantaOutline,
                focusedContainerColor = VantaSurface,
                unfocusedContainerColor = VantaSurface,
                focusedTextColor = VantaTextPrimary,
                unfocusedTextColor = VantaTextPrimary
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(50.dp)
                .testTag("library_search_input")
        )

        Spacer(Modifier.height(12.dp))

        // Category Filter Chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(LibraryFilter.entries) { filter ->
                val isSelected = selectedFilter == filter
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter.label, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = VantaPrimary,
                        selectedLabelColor = Color.White,
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

        Spacer(Modifier.height(14.dp))

        // Content Display (Grid vs List)
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No media found",
                        color = VantaTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "No content matches \"${selectedFilter.label}\".",
                        color = VantaTextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = onNavigateToImport,
                        colors = ButtonDefaults.buttonColors(containerColor = VantaPrimary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Import Video")
                    }
                }
            }
        } else if (isGridView) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(115.dp),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredList) { item ->
                    MediaPosterCard(
                        media = item,
                        onClick = { onSelectMedia(item) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredList) { item ->
                    MediaListItemCard(media = item, onClick = { onSelectMedia(item) })
                }
            }
        }
    }
}

@Composable
private fun MediaListItemCard(
    media: MediaEntity,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = VantaSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("list_item_${media.id}")
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Poster thumbnail
            Box(
                modifier = Modifier
                    .width(70.dp)
                    .aspectRatio(2f / 3f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(VantaSurfaceElevated)
            ) {
                if (media.posterUri != null) {
                    AsyncImage(
                        model = media.posterUri,
                        contentDescription = media.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(Modifier.width(14.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = VantaPrimary.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = media.type,
                            color = VantaPrimary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = media.releaseYear.toString(),
                        color = VantaTextSecondary,
                        fontSize = 11.sp
                    )
                    if (media.rating > 0) {
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.Star, contentDescription = null, tint = VantaAccentGold, modifier = Modifier.size(11.dp))
                        Spacer(Modifier.width(2.dp))
                        Text(
                            text = String.format("%.1f", media.rating),
                            color = VantaAccentGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    text = media.title,
                    color = VantaTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(2.dp))

                Text(
                    text = media.genre,
                    color = VantaAccentCyan,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (media.playbackPositionMs > 0 && !media.isCompleted) {
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { media.progressFraction },
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
}
