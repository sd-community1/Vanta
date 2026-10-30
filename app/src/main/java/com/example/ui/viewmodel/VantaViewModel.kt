package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.VantaDatabase
import com.example.data.model.EpisodeEntity
import com.example.data.model.MediaEntity
import com.example.data.model.MediaType
import com.example.data.model.SeasonEntity
import com.example.data.remote.NoOpRemoteContentDataSource
import com.example.data.repository.LocalMediaRepository
import com.example.data.repository.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class VantaScreen {
    data object Home : VantaScreen()
    data object Library : VantaScreen()
    data object Search : VantaScreen()
    data object CineAI : VantaScreen()
    data object Settings : VantaScreen()
    data object Import : VantaScreen()
    data class Detail(val mediaId: String) : VantaScreen()
    data class Player(
        val videoUri: String,
        val title: String,
        val subtitle: String = "",
        val positionMs: Long = 0L,
        val mediaId: String? = null,
        val episodeId: String? = null
    ) : VantaScreen()
}

class VantaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MediaRepository

    init {
        val db = VantaDatabase.getInstance(application)
        val remoteSource = NoOpRemoteContentDataSource()
        repository = LocalMediaRepository(
            mediaDao = db.mediaDao(),
            seasonDao = db.seasonDao(),
            episodeDao = db.episodeDao(),
            remoteDataSource = remoteSource
        )
    }

    val allMedia: StateFlow<List<MediaEntity>> = repository.getAllMedia()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val continueWatching: StateFlow<List<MediaEntity>> = repository.getContinueWatching()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentlyAdded: StateFlow<List<MediaEntity>> = repository.getRecentlyAdded(10)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val screenBackStack = mutableListOf<VantaScreen>(VantaScreen.Home)
    private val _currentScreen = MutableStateFlow<VantaScreen>(VantaScreen.Home)
    val currentScreen: StateFlow<VantaScreen> = _currentScreen.asStateFlow()

    fun navigateTo(screen: VantaScreen) {
        if (_currentScreen.value != screen) {
            screenBackStack.add(screen)
            _currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        if (screenBackStack.size > 1) {
            screenBackStack.removeAt(screenBackStack.size - 1)
            _currentScreen.value = screenBackStack.last()
            return true
        }
        return false
    }

    fun getMediaById(id: String) = repository.getMediaById(id)
    fun getSeasonsForSeries(seriesId: String) = repository.getSeasons(seriesId)
    fun getEpisodesForSeries(seriesId: String) = repository.getEpisodes(seriesId)

    fun saveMediaWithContent(
        media: MediaEntity,
        seasons: List<SeasonEntity>,
        episodes: List<EpisodeEntity>
    ) {
        viewModelScope.launch {
            repository.saveMedia(media)
            seasons.forEach { repository.saveSeason(it) }
            episodes.forEach { repository.saveEpisode(it) }
            navigateTo(VantaScreen.Detail(media.id))
        }
    }

    fun updateMedia(media: MediaEntity) {
        viewModelScope.launch {
            repository.saveMedia(media)
        }
    }

    fun deleteMedia(mediaId: String) {
        viewModelScope.launch {
            repository.deleteMedia(mediaId)
            navigateBack()
        }
    }

    fun addEpisode(episode: EpisodeEntity) {
        viewModelScope.launch {
            repository.saveEpisode(episode)
        }
    }

    fun deleteEpisode(episodeId: String) {
        viewModelScope.launch {
            repository.deleteEpisode(episodeId)
        }
    }

    fun toggleFavorite(mediaId: String, isFavorite: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(mediaId, isFavorite)
        }
    }

    fun updatePlaybackProgress(
        mediaId: String?,
        episodeId: String?,
        positionMs: Long,
        durationMs: Long,
        isCompleted: Boolean
    ) {
        viewModelScope.launch {
            if (episodeId != null) {
                repository.updateEpisodePlaybackPosition(episodeId, positionMs, durationMs, isCompleted)
            } else if (mediaId != null) {
                repository.updatePlaybackPosition(mediaId, positionMs, durationMs, isCompleted)
            }
        }
    }

    fun loadDemoShowcase() {
        viewModelScope.launch {
            repository.loadDemoShowcase()
        }
    }

    fun clearDemoShowcase() {
        viewModelScope.launch {
            repository.clearDemoShowcase()
        }
    }
}
