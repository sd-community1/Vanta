package com.example.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MediaEntity
import com.example.ui.components.MediaPosterCard
import com.example.ui.theme.VantaBackground
import com.example.ui.theme.VantaOutline
import com.example.ui.theme.VantaPrimary
import com.example.ui.theme.VantaSurface
import com.example.ui.theme.VantaSurfaceVariant
import com.example.ui.theme.VantaTextPrimary
import com.example.ui.theme.VantaTextSecondary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    allMedia: List<MediaEntity>,
    onSelectMedia: (MediaEntity) -> Unit,
    onNavigateBack: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val genres = listOf("Action", "Cyberpunk", "Anime", "Sci-Fi", "Thriller", "Drama", "Adventure")

    val searchResults = if (query.isBlank()) {
        emptyList()
    } else {
        allMedia.filter { item ->
            item.title.contains(query, ignoreCase = true) ||
                    item.genre.contains(query, ignoreCase = true) ||
                    item.description.contains(query, ignoreCase = true) ||
                    item.type.contains(query, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VantaBackground)
            .testTag("search_screen")
    ) {
        TopAppBar(
            title = {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search titles, genres, series...", fontSize = 14.sp) },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
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
                        .padding(end = 12.dp)
                        .height(48.dp)
                        .testTag("search_text_input")
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = VantaTextPrimary)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = VantaBackground)
        )

        Spacer(Modifier.height(12.dp))

        if (query.isBlank()) {
            // Genre Suggestions
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = "Explore Popular Genres",
                    color = VantaTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    genres.forEach { g ->
                        AssistChip(
                            onClick = { query = g },
                            label = { Text(g, fontSize = 12.sp, color = VantaTextPrimary) },
                            colors = AssistChipDefaults.assistChipColors(containerColor = VantaSurfaceVariant),
                            border = AssistChipDefaults.assistChipBorder(borderColor = VantaOutline, enabled = true)
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))

                Text(
                    text = "Recently Added in Library",
                    color = VantaTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(12.dp))

            LazyVerticalGrid(
                columns = GridCells.Adaptive(115.dp),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(allMedia.take(12)) { item ->
                    MediaPosterCard(media = item, onClick = { onSelectMedia(item) }, modifier = Modifier.fillMaxWidth())
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Results for \"$query\"",
                    color = VantaTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${searchResults.size} found",
                    color = VantaTextSecondary,
                    fontSize = 12.sp
                )
            }

            Spacer(Modifier.height(10.dp))

            if (searchResults.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = VantaTextSecondary, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "No titles found matching \"$query\"",
                            color = VantaTextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(115.dp),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(searchResults) { item ->
                        MediaPosterCard(media = item, onClick = { onSelectMedia(item) }, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }
}
