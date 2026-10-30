package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.MediaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {
    @Query("SELECT * FROM media_items ORDER BY addedDate DESC")
    fun getAllMedia(): Flow<List<MediaEntity>>

    @Query("SELECT * FROM media_items WHERE id = :id LIMIT 1")
    fun getMediaById(id: String): Flow<MediaEntity?>

    @Query("SELECT * FROM media_items WHERE id = :id LIMIT 1")
    suspend fun getMediaByIdSync(id: String): MediaEntity?

    @Query("SELECT * FROM media_items WHERE type = :type ORDER BY addedDate DESC")
    fun getMediaByType(type: String): Flow<List<MediaEntity>>

    @Query("SELECT * FROM media_items WHERE playbackPositionMs > 0 AND isCompleted = 0 ORDER BY lastWatchedDate DESC")
    fun getContinueWatchingMedia(): Flow<List<MediaEntity>>

    @Query("SELECT * FROM media_items WHERE isFavorite = 1 ORDER BY addedDate DESC")
    fun getFavorites(): Flow<List<MediaEntity>>

    @Query("SELECT * FROM media_items ORDER BY addedDate DESC LIMIT :limit")
    fun getRecentlyAdded(limit: Int = 10): Flow<List<MediaEntity>>

    @Query("""
        SELECT * FROM media_items 
        WHERE title LIKE '%' || :query || '%' 
           OR genre LIKE '%' || :query || '%' 
           OR description LIKE '%' || :query || '%' 
        ORDER BY title ASC
    """)
    fun searchMedia(query: String): Flow<List<MediaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(media: MediaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(mediaList: List<MediaEntity>)

    @Update
    suspend fun updateMedia(media: MediaEntity)

    @Delete
    suspend fun deleteMedia(media: MediaEntity)

    @Query("DELETE FROM media_items WHERE id = :id")
    suspend fun deleteMediaById(id: String)

    @Query("""
        UPDATE media_items 
        SET playbackPositionMs = :positionMs, 
            durationMs = CASE WHEN durationMs <= 0 THEN :durationMs ELSE durationMs END,
            isCompleted = :isCompleted, 
            lastWatchedDate = :timestamp 
        WHERE id = :id
    """)
    suspend fun updatePlaybackPosition(
        id: String,
        positionMs: Long,
        durationMs: Long,
        isCompleted: Boolean,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("UPDATE media_items SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: String, isFavorite: Boolean)

    @Query("SELECT COUNT(*) FROM media_items")
    fun getMediaCount(): Flow<Int>
}
