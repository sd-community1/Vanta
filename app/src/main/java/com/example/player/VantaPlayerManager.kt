package com.example.player

import android.content.Context
import android.net.Uri
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class VantaPlayerManager(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private var exoPlayer: ExoPlayer? = null
    private var progressJob: Job? = null
    private var onProgressUpdateCallback: ((positionMs: Long, durationMs: Long, isCompleted: Boolean) -> Unit)? = null

    private val _playerState = MutableStateFlow(VantaPlayerState())
    val playerState: StateFlow<VantaPlayerState> = _playerState.asStateFlow()

    fun getPlayer(): ExoPlayer {
        return exoPlayer ?: createPlayer().also { exoPlayer = it }
    }

    private fun createPlayer(): ExoPlayer {
        val player = ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    val isBuffering = playbackState == Player.STATE_BUFFERING
                    val isEnded = playbackState == Player.STATE_ENDED
                    val duration = if (duration > 0) duration else 0L

                    _playerState.value = _playerState.value.copy(
                        isBuffering = isBuffering,
                        isEnded = isEnded,
                        durationMs = duration
                    )

                    if (isEnded) {
                        onProgressUpdateCallback?.invoke(duration, duration, true)
                    }
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _playerState.value = _playerState.value.copy(isPlaying = isPlaying)
                    if (isPlaying) {
                        startProgressTracking()
                    } else {
                        stopProgressTracking()
                        val current = exoPlayer?.currentPosition ?: 0L
                        val dur = exoPlayer?.duration?.coerceAtLeast(0L) ?: 0L
                        onProgressUpdateCallback?.invoke(current, dur, false)
                    }
                }

                override fun onTracksChanged(tracks: Tracks) {
                    extractTracks(tracks)
                }

                override fun onPlayerError(error: PlaybackException) {
                    _playerState.value = _playerState.value.copy(
                        isBuffering = false,
                        isPlaying = false,
                        errorMessage = error.localizedMessage ?: "Playback error encountered"
                    )
                }
            })
        }
        return player
    }

    fun prepare(
        uri: Uri,
        initialPositionMs: Long = 0L,
        onProgressUpdate: (positionMs: Long, durationMs: Long, isCompleted: Boolean) -> Unit
    ) {
        this.onProgressUpdateCallback = onProgressUpdate
        val player = getPlayer()

        _playerState.value = VantaPlayerState(isBuffering = true)

        val mediaItem = MediaItem.fromUri(uri)
        player.setMediaItem(mediaItem)
        if (initialPositionMs > 0) {
            player.seekTo(initialPositionMs)
        }
        player.prepare()
        player.play()
    }

    private fun startProgressTracking() {
        progressJob?.cancel()
        progressJob = scope.launch(Dispatchers.Main) {
            while (isActive) {
                exoPlayer?.let { player ->
                    val pos = player.currentPosition
                    val dur = player.duration.coerceAtLeast(0L)
                    val buffered = player.bufferedPosition

                    _playerState.value = _playerState.value.copy(
                        currentPositionMs = pos,
                        durationMs = dur,
                        bufferedPositionMs = buffered
                    )

                    // Periodic persistence
                    onProgressUpdateCallback?.invoke(pos, dur, false)
                }
                delay(1000)
            }
        }
    }

    private fun stopProgressTracking() {
        progressJob?.cancel()
        progressJob = null
    }

    fun play() {
        exoPlayer?.play()
    }

    fun pause() {
        exoPlayer?.pause()
    }

    fun togglePlayPause() {
        exoPlayer?.let {
            if (it.isPlaying) it.pause() else it.play()
        }
    }

    fun seekTo(positionMs: Long) {
        exoPlayer?.seekTo(positionMs.coerceAtLeast(0L))
        _playerState.value = _playerState.value.copy(currentPositionMs = positionMs)
    }

    fun seekRelative(deltaMs: Long) {
        exoPlayer?.let {
            val target = (it.currentPosition + deltaMs).coerceIn(0L, it.duration.coerceAtLeast(0L))
            it.seekTo(target)
            _playerState.value = _playerState.value.copy(
                currentPositionMs = target,
                seekDeltaIndicator = (deltaMs / 1000).toInt()
            )
            scope.launch {
                delay(650)
                _playerState.value = _playerState.value.copy(seekDeltaIndicator = null)
            }
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        exoPlayer?.playbackParameters = PlaybackParameters(speed)
        _playerState.value = _playerState.value.copy(playbackSpeed = speed)
    }

    fun setControlsVisible(visible: Boolean) {
        if (!_playerState.value.isLocked) {
            _playerState.value = _playerState.value.copy(areControlsVisible = visible)
        }
    }

    fun toggleControlsVisibility() {
        if (!_playerState.value.isLocked) {
            _playerState.value = _playerState.value.copy(areControlsVisible = !_playerState.value.areControlsVisible)
        }
    }

    fun toggleLock() {
        val newLocked = !_playerState.value.isLocked
        _playerState.value = _playerState.value.copy(
            isLocked = newLocked,
            areControlsVisible = !newLocked
        )
    }

    fun cycleResizeMode() {
        val nextMode = (_playerState.value.resizeModeIndex + 1) % 3
        _playerState.value = _playerState.value.copy(resizeModeIndex = nextMode)
    }

    fun setBrightnessIndicator(level: Float?) {
        _playerState.value = _playerState.value.copy(gestureBrightness = level)
    }

    fun setVolumeIndicator(level: Float?) {
        _playerState.value = _playerState.value.copy(gestureVolume = level)
    }

    private fun extractTracks(tracks: Tracks) {
        val audioList = mutableListOf<VantaTrackInfo>()
        val subtitleList = mutableListOf<VantaTrackInfo>()

        for ((groupIndex, group) in tracks.groups.withIndex()) {
            val trackType = group.type
            val mediaTrackGroup = group.mediaTrackGroup

            for (trackIndex in 0 until mediaTrackGroup.length) {
                val format = mediaTrackGroup.getFormat(trackIndex)
                val isSelected = group.isTrackSelected(trackIndex)
                val lang = format.language ?: "Undetermined"
                val label = format.label ?: (if (lang.isNotBlank() && lang != "und") lang.uppercase() else "Track ${trackIndex + 1}")
                val desc = "${format.sampleMimeType ?: ""} ${if (format.bitrate > 0) "${format.bitrate / 1000}kbps" else ""}".trim()

                val info = VantaTrackInfo(
                    groupIndex = groupIndex,
                    trackIndex = trackIndex,
                    label = label,
                    language = lang,
                    isSelected = isSelected,
                    formatDescription = desc
                )

                if (trackType == C.TRACK_TYPE_AUDIO) {
                    audioList.add(info)
                } else if (trackType == C.TRACK_TYPE_TEXT) {
                    subtitleList.add(info)
                }
            }
        }

        _playerState.value = _playerState.value.copy(
            audioTracks = audioList,
            subtitleTracks = subtitleList
        )
    }

    fun selectAudioTrack(trackInfo: VantaTrackInfo) {
        val player = exoPlayer ?: return
        val groups = player.currentTracks.groups
        if (trackInfo.groupIndex < groups.size) {
            val targetGroup = groups[trackInfo.groupIndex].mediaTrackGroup
            player.trackSelectionParameters = player.trackSelectionParameters
                .buildUpon()
                .setOverrideForType(TrackSelectionOverride(targetGroup, trackInfo.trackIndex))
                .build()
        }
    }

    fun selectSubtitleTrack(trackInfo: VantaTrackInfo) {
        val player = exoPlayer ?: return
        val groups = player.currentTracks.groups
        if (trackInfo.groupIndex < groups.size) {
            val targetGroup = groups[trackInfo.groupIndex].mediaTrackGroup
            player.trackSelectionParameters = player.trackSelectionParameters
                .buildUpon()
                .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                .setOverrideForType(TrackSelectionOverride(targetGroup, trackInfo.trackIndex))
                .build()
            _playerState.value = _playerState.value.copy(isSubtitlesEnabled = true)
        }
    }

    fun disableSubtitles() {
        val player = exoPlayer ?: return
        player.trackSelectionParameters = player.trackSelectionParameters
            .buildUpon()
            .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
            .build()
        _playerState.value = _playerState.value.copy(isSubtitlesEnabled = false)
    }

    fun release() {
        stopProgressTracking()
        val current = exoPlayer?.currentPosition ?: 0L
        val dur = exoPlayer?.duration?.coerceAtLeast(0L) ?: 0L
        onProgressUpdateCallback?.invoke(current, dur, false)
        exoPlayer?.release()
        exoPlayer = null
    }
}
