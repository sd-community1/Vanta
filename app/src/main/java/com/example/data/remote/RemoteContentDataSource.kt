package com.example.data.remote

import com.example.data.model.EpisodeEntity
import com.example.data.model.MediaEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/**
 * Future Remote Data Layer Interface (Section 9 Architecture Contract).
 * Designed for future cloud sync/hosting integration without altering UI or Domain layers.
 */
interface RemoteContentDataSource {
    fun fetchRemoteCatalog(): Flow<List<MediaEntity>>
    suspend fun syncPlaybackProgress(mediaId: String, positionMs: Long, isCompleted: Boolean): Boolean
    suspend fun backupLibrary(items: List<MediaEntity>, episodes: List<EpisodeEntity>): Result<Unit>
}

/**
 * Default offline implementation ensuring 100% local, self-contained operation.
 */
class NoOpRemoteContentDataSource : RemoteContentDataSource {
    override fun fetchRemoteCatalog(): Flow<List<MediaEntity>> = emptyFlow()
    override suspend fun syncPlaybackProgress(mediaId: String, positionMs: Long, isCompleted: Boolean): Boolean = true
    override suspend fun backupLibrary(items: List<MediaEntity>, episodes: List<EpisodeEntity>): Result<Unit> {
        return Result.success(Unit)
    }
}
