package com.example.data.model

enum class MediaType {
    MOVIE,
    SERIES,
    ANIME;

    companion object {
        fun fromString(value: String): MediaType {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: MOVIE
        }
    }
}
