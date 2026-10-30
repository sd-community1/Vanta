package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.EpisodeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EpisodeDao {
    @Query("SELECT * FROM episodes WHERE seriesId = :seriesId ORDER BY seasonNumber ASC, episodeNumber ASC")
    fun getEpisodesForSeries(seriesId: String): Flow<List<EpisodeEntity>>

    @Query("SELECT * FROM episodes WHERE seriesId = :seriesId AND seasonNumber = :seasonNumber ORDER BY episodeNumber ASC")
    fun getEpisodesForSeason(seriesId: String, seasonNumber: Int): Flow<List<EpisodeEntity>>

    @Query("SELECT * FROM episodes WHERE id = :id LIMIT 1")
    fun getEpisodeById(id: String): Flow<EpisodeEntity?>

    @Query("SELECT * FROM episodes WHERE id = :id LIMIT 1")
    suspend fun getEpisodeByIdSync(id: String): EpisodeEntity?

    @Query("SELECT * FROM episodes WHERE playbackPositionMs > 0 AND isCompleted = 0 ORDER BY lastWatchedDate DESC")
    fun getContinueWatchingEpisodes(): Flow<List<EpisodeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisode(episode: EpisodeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisodes(episodes: List<EpisodeEntity>)

    @Update
    suspend fun updateEpisode(episode: EpisodeEntity)

    @Delete
    suspend fun deleteEpisode(episode: EpisodeEntity)

    @Query("DELETE FROM episodes WHERE id = :id")
    suspend fun deleteEpisodeById(id: String)

    @Query("DELETE FROM episodes WHERE seriesId = :seriesId")
    suspend fun deleteEpisodesForSeries(seriesId: String)

    @Query("""
        UPDATE episodes 
        SET playbackPositionMs = :positionMs, 
            durationMs = CASE WHEN durationMs <= 0 THEN :durationMs ELSE durationMs END,
            isCompleted = :isCompleted, 
            lastWatchedDate = :timestamp 
        WHERE id = :id
    """)
    suspend fun updateEpisodeProgress(
        id: String,
        positionMs: Long,
        durationMs: Long,
        isCompleted: Boolean,
        timestamp: Long = System.currentTimeMillis()
    )
}
