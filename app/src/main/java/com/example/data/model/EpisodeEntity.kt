package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "episodes",
    foreignKeys = [
        ForeignKey(
            entity = MediaEntity::class,
            parentColumns = ["id"],
            childColumns = ["seriesId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["seriesId"]),
        Index(value = ["seriesId", "seasonNumber", "episodeNumber"])
    ]
)
data class EpisodeEntity(
    @PrimaryKey
    val id: String,
    val seriesId: String,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val title: String,
    val description: String = "",
    val thumbnailUri: String? = null,
    val videoUri: String,
    val durationMs: Long = 0L,
    val playbackPositionMs: Long = 0L,
    val lastWatchedDate: Long? = null,
    val isCompleted: Boolean = false,
    val addedDate: Long = System.currentTimeMillis()
) {
    val progressFraction: Float
        get() = if (durationMs > 0) (playbackPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
}
