package com.example.data.repository

import com.example.data.model.EpisodeEntity
import com.example.data.model.MediaEntity
import com.example.data.model.MediaType
import com.example.data.model.SeasonEntity
import kotlinx.coroutines.flow.Flow

interface MediaRepository {
    fun getAllMedia(): Flow<List<MediaEntity>>
    fun getMediaById(id: String): Flow<MediaEntity?>
    suspend fun getMediaByIdSync(id: String): MediaEntity?
    fun getMediaByType(type: MediaType): Flow<List<MediaEntity>>
    fun getContinueWatching(): Flow<List<MediaEntity>>
    fun getFavorites(): Flow<List<MediaEntity>>
    fun getRecentlyAdded(limit: Int = 10): Flow<List<MediaEntity>>
    fun searchMedia(query: String): Flow<List<MediaEntity>>
    fun getMediaCount(): Flow<Int>

    // Seasons & Episodes
    fun getSeasons(seriesId: String): Flow<List<SeasonEntity>>
    fun getEpisodes(seriesId: String): Flow<List<EpisodeEntity>>
    fun getEpisodesForSeason(seriesId: String, seasonNumber: Int): Flow<List<EpisodeEntity>>
    fun getEpisodeById(id: String): Flow<EpisodeEntity?>
    suspend fun getEpisodeByIdSync(id: String): EpisodeEntity?
    fun getContinueWatchingEpisodes(): Flow<List<EpisodeEntity>>

    // Mutations
    suspend fun saveMedia(media: MediaEntity)
    suspend fun deleteMedia(mediaId: String)
    suspend fun saveSeason(season: SeasonEntity)
    suspend fun saveEpisode(episode: EpisodeEntity)
    suspend fun deleteEpisode(episodeId: String)
    suspend fun updatePlaybackPosition(mediaId: String, positionMs: Long, durationMs: Long, isCompleted: Boolean)
    suspend fun updateEpisodePlaybackPosition(episodeId: String, positionMs: Long, durationMs: Long, isCompleted: Boolean)
    suspend fun toggleFavorite(mediaId: String, isFavorite: Boolean)

    // Demo showcase
    suspend fun loadDemoShowcase()
    suspend fun clearDemoShowcase()
}
