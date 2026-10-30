package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "seasons",
    foreignKeys = [
        ForeignKey(
            entity = MediaEntity::class,
            parentColumns = ["id"],
            childColumns = ["seriesId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["seriesId", "seasonNumber"], unique = true)]
)
data class SeasonEntity(
    @PrimaryKey
    val id: String,
    val seriesId: String,
    val seasonNumber: Int,
    val title: String = "Season $seasonNumber",
    val overview: String? = null
)
