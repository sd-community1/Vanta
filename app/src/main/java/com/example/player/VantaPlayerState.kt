package com.example.player

import androidx.media3.common.Tracks

data class VantaTrackInfo(
    val groupIndex: Int,
    val trackIndex: Int,
    val label: String,
    val language: String,
    val isSelected: Boolean,
    val formatDescription: String
)

data class VantaPlayerState(
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val bufferedPositionMs: Long = 0L,
    val isBuffering: Boolean = false,
    val playbackSpeed: Float = 1.0f,
    val isLocked: Boolean = false,
    val areControlsVisible: Boolean = true,
    val gestureBrightness: Float? = null, // 0..1 when dragging
    val gestureVolume: Float? = null, // 0..1 when dragging
    val seekDeltaIndicator: Int? = null, // e.g. -10 or +10
    val audioTracks: List<VantaTrackInfo> = emptyList(),
    val subtitleTracks: List<VantaTrackInfo> = emptyList(),
    val isSubtitlesEnabled: Boolean = true,
    val errorMessage: String? = null,
    val isEnded: Boolean = false,
    val resizeModeIndex: Int = 0 // 0 = Fit, 1 = Zoom/Crop, 2 = Fill/Stretch
)
