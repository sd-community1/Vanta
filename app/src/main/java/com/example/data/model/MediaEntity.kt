package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "media_items")
data class MediaEntity(
    @PrimaryKey
    val id: String,
    val type: String, // MOVIE, SERIES, ANIME
    val title: String,
    val description: String = "",
    val releaseYear: Int = 2024,
    val genre: String = "Sci-Fi, Action",
    val posterUri: String? = null,
    val backdropUri: String? = null,
    val videoUri: String? = null, // for movies
    val durationMs: Long = 0L,
    val isFavorite: Boolean = false,
    val addedDate: Long = System.currentTimeMillis(),
    val lastWatchedDate: Long? = null,
    val playbackPositionMs: Long = 0L,
    val isCompleted: Boolean = false,
    val rating: Float = 8.5f
) {
    val mediaType: MediaType
        get() = MediaType.fromString(type)

    val progressFraction: Float
        get() = if (durationMs > 0) (playbackPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
}
