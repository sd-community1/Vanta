package com.example.data.repository

import com.example.data.dao.EpisodeDao
import com.example.data.dao.MediaDao
import com.example.data.dao.SeasonDao
import com.example.data.model.EpisodeEntity
import com.example.data.model.MediaEntity
import com.example.data.model.MediaType
import com.example.data.model.SeasonEntity
import com.example.data.remote.RemoteContentDataSource
import kotlinx.coroutines.flow.Flow

class LocalMediaRepository(
    private val mediaDao: MediaDao,
    private val seasonDao: SeasonDao,
    private val episodeDao: EpisodeDao,
    private val remoteDataSource: RemoteContentDataSource
) : MediaRepository {

    override fun getAllMedia(): Flow<List<MediaEntity>> = mediaDao.getAllMedia()

    override fun getMediaById(id: String): Flow<MediaEntity?> = mediaDao.getMediaById(id)

    override suspend fun getMediaByIdSync(id: String): MediaEntity? = mediaDao.getMediaByIdSync(id)

    override fun getMediaByType(type: MediaType): Flow<List<MediaEntity>> =
        mediaDao.getMediaByType(type.name)

    override fun getContinueWatching(): Flow<List<MediaEntity>> = mediaDao.getContinueWatchingMedia()

    override fun getFavorites(): Flow<List<MediaEntity>> = mediaDao.getFavorites()

    override fun getRecentlyAdded(limit: Int): Flow<List<MediaEntity>> =
        mediaDao.getRecentlyAdded(limit)

    override fun searchMedia(query: String): Flow<List<MediaEntity>> = mediaDao.searchMedia(query)

    override fun getMediaCount(): Flow<Int> = mediaDao.getMediaCount()

    override fun getSeasons(seriesId: String): Flow<List<SeasonEntity>> =
        seasonDao.getSeasonsForSeries(seriesId)

    override fun getEpisodes(seriesId: String): Flow<List<EpisodeEntity>> =
        episodeDao.getEpisodesForSeries(seriesId)

    override fun getEpisodesForSeason(seriesId: String, seasonNumber: Int): Flow<List<EpisodeEntity>> =
        episodeDao.getEpisodesForSeason(seriesId, seasonNumber)

    override fun getEpisodeById(id: String): Flow<EpisodeEntity?> =
        episodeDao.getEpisodeById(id)

    override suspend fun getEpisodeByIdSync(id: String): EpisodeEntity? =
        episodeDao.getEpisodeByIdSync(id)

    override fun getContinueWatchingEpisodes(): Flow<List<EpisodeEntity>> =
        episodeDao.getContinueWatchingEpisodes()

    override suspend fun saveMedia(media: MediaEntity) {
        mediaDao.insertMedia(media)
    }

    override suspend fun deleteMedia(mediaId: String) {
        // Cascade delete child episodes and seasons first
        episodeDao.deleteEpisodesForSeries(mediaId)
        seasonDao.deleteSeasonsForSeries(mediaId)
        mediaDao.deleteMediaById(mediaId)
    }

    override suspend fun saveSeason(season: SeasonEntity) {
        seasonDao.insertSeason(season)
    }

    override suspend fun saveEpisode(episode: EpisodeEntity) {
        episodeDao.insertEpisode(episode)
    }

    override suspend fun deleteEpisode(episodeId: String) {
        episodeDao.deleteEpisodeById(episodeId)
    }

    override suspend fun updatePlaybackPosition(
        mediaId: String,
        positionMs: Long,
        durationMs: Long,
        isCompleted: Boolean
    ) {
        mediaDao.updatePlaybackPosition(
            id = mediaId,
            positionMs = positionMs,
            durationMs = durationMs,
            isCompleted = isCompleted
        )
        // Prepared for future cloud sync
        remoteDataSource.syncPlaybackProgress(mediaId, positionMs, isCompleted)
    }

    override suspend fun updateEpisodePlaybackPosition(
        episodeId: String,
        positionMs: Long,
        durationMs: Long,
        isCompleted: Boolean
    ) {
        episodeDao.updateEpisodeProgress(
            id = episodeId,
            positionMs = positionMs,
            durationMs = durationMs,
            isCompleted = isCompleted
        )
    }

    override suspend fun toggleFavorite(mediaId: String, isFavorite: Boolean) {
        mediaDao.setFavorite(mediaId, isFavorite)
    }

    override suspend fun loadDemoShowcase() {
        val demoMovie = MediaEntity(
            id = "demo_movie_shadow_grid",
            type = MediaType.MOVIE.name,
            title = "[DEMO] Shadow Grid: 2049",
            description = "In the rain-drenched neon metropolis of Neo-Tokyo, a rogue cybernetic detective uncovers an encrypted conspiracy within the city's neural backbone.",
            releaseYear = 2024,
            genre = "Cyberpunk, Sci-Fi, Thriller",
            posterUri = "android.resource://com.aistudio.vanta.stream/drawable/vanta_demo_poster",
            backdropUri = "android.resource://com.aistudio.vanta.stream/drawable/vanta_hero_backdrop",
            videoUri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            durationMs = 596000L,
            playbackPositionMs = 142000L, // Show active continue watching
            isFavorite = true,
            rating = 9.4f
        )

        val demoSeries = MediaEntity(
            id = "demo_series_neon_chronicles",
            type = MediaType.ANIME.name,
            title = "[DEMO] Cyber City: Edge of Horizon",
            description = "High-octane anime following a syndicate courier navigating underworld contracts across Neo-Tokyo's subterranean sectors.",
            releaseYear = 2025,
            genre = "Anime, Action, Cyberpunk",
            posterUri = "android.resource://com.aistudio.vanta.stream/drawable/vanta_demo_poster",
            backdropUri = "android.resource://com.aistudio.vanta.stream/drawable/vanta_hero_backdrop",
            durationMs = 0L,
            isFavorite = true,
            rating = 9.8f
        )

        val demoSeason1 = SeasonEntity(
            id = "demo_season_1",
            seriesId = demoSeries.id,
            seasonNumber = 1,
            title = "Season 1: Neon Genesis",
            overview = "The initiation contracts that ignited the corporate conflict."
        )

        val demoSeason2 = SeasonEntity(
            id = "demo_season_2",
            seriesId = demoSeries.id,
            seasonNumber = 2,
            title = "Season 2: Neural Breach",
            overview = "The infiltration into the central mainframe."
        )

        val ep1 = EpisodeEntity(
            id = "demo_ep_1",
            seriesId = demoSeries.id,
            seasonNumber = 1,
            episodeNumber = 1,
            title = "Episode 1: Ghost in the Protocol",
            description = "A routine data delivery takes a lethal turn when an automated hunter-drone ambushes the dropsite.",
            thumbnailUri = "android.resource://com.aistudio.vanta.stream/drawable/vanta_hero_backdrop",
            videoUri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
            durationMs = 653000L,
            playbackPositionMs = 210000L
        )

        val ep2 = EpisodeEntity(
            id = "demo_ep_2",
            seriesId = demoSeries.id,
            seasonNumber = 1,
            episodeNumber = 2,
            title = "Episode 2: Terminal Velocity",
            description = "Pursued by elite security forces, the team seeks sanctuary inside a decommissioned maglev conduit.",
            thumbnailUri = "android.resource://com.aistudio.vanta.stream/drawable/vanta_hero_backdrop",
            videoUri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            durationMs = 596000L,
            playbackPositionMs = 0L
        )

        val ep3 = EpisodeEntity(
            id = "demo_ep_3",
            seriesId = demoSeries.id,
            seasonNumber = 2,
            episodeNumber = 1,
            title = "Episode 1: The Zero Vector",
            description = "With the syndicate compromised, deep-underground black markets become the only path to survival.",
            thumbnailUri = "android.resource://com.aistudio.vanta.stream/drawable/vanta_hero_backdrop",
            videoUri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
            durationMs = 653000L,
            playbackPositionMs = 0L
        )

        mediaDao.insertMedia(demoMovie)
        mediaDao.insertMedia(demoSeries)
        seasonDao.insertSeasons(listOf(demoSeason1, demoSeason2))
        episodeDao.insertEpisodes(listOf(ep1, ep2, ep3))
    }

    override suspend fun clearDemoShowcase() {
        deleteMedia("demo_movie_shadow_grid")
        deleteMedia("demo_series_neon_chronicles")
    }
}
