package com.example.player

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.ui.theme.VantaBackground
import com.example.ui.theme.VantaPrimary
import com.example.ui.theme.VantaSurfaceVariant
import com.example.ui.theme.VantaTextPrimary
import com.example.ui.theme.VantaTextSecondary
import com.example.util.StorageAccessHelper
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class)
@ExperimentalMaterial3Api
@Composable
fun VantaPlayerScreen(
    videoUriString: String,
    title: String,
    subTitle: String = "",
    initialPositionMs: Long = 0L,
    onProgressUpdate: (positionMs: Long, durationMs: Long, isCompleted: Boolean) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val coroutineScope = rememberCoroutineScope()

    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val playerManager = remember { VantaPlayerManager(context, coroutineScope) }
    val playerState by playerManager.playerState.collectAsState()

    var showSpeedDialog by remember { mutableStateOf(false) }
    var showTrackSheet by remember { mutableStateOf(false) }
    var isFileAccessible by remember { mutableStateOf(true) }

    // Check accessibility
    LaunchedEffect(videoUriString) {
        isFileAccessible = StorageAccessHelper.isUriAccessible(context, videoUriString)
        if (isFileAccessible) {
            val uri = Uri.parse(videoUriString)
            StorageAccessHelper.takePersistableUriPermission(context, uri)
            playerManager.prepare(uri, initialPositionMs, onProgressUpdate)
        }
    }

    // Auto-hide controls timer
    LaunchedEffect(playerState.areControlsVisible, playerState.isPlaying, playerState.isLocked) {
        if (playerState.areControlsVisible && playerState.isPlaying && !playerState.isLocked) {
            delay(4000)
            playerManager.setControlsVisible(false)
        }
    }

    // Release player on exit
    DisposableEffect(Unit) {
        onDispose {
            playerManager.release()
        }
    }

    BackHandler {
        onNavigateBack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VantaBackground)
            .testTag("player_container")
    ) {
        if (!isFileAccessible) {
            InaccessibleFileCard(
                title = title,
                onBack = onNavigateBack
            )
        } else {
            // Video Surface
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        useController = false
                        player = playerManager.getPlayer()
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        resizeMode = when (playerState.resizeModeIndex) {
                            1 -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                            2 -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                            else -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                        }
                    }
                },
                update = { view ->
                    view.resizeMode = when (playerState.resizeModeIndex) {
                        1 -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        2 -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                        else -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Gesture Detector Layer (Tap & Drag)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(playerState.isLocked) {
                        detectTapGestures(
                            onTap = {
                                playerManager.toggleControlsVisibility()
                            },
                            onDoubleTap = { offset ->
                                if (!playerState.isLocked) {
                                    val screenWidth = size.width
                                    if (offset.x < screenWidth * 0.4f) {
                                        playerManager.seekRelative(-10_000L)
                                    } else if (offset.x > screenWidth * 0.6f) {
                                        playerManager.seekRelative(10_000L)
                                    } else {
                                        playerManager.togglePlayPause()
                                    }
                                }
                            }
                        )
                    }
                    .pointerInput(playerState.isLocked) {
                        if (!playerState.isLocked) {
                            detectDragGestures(
                                onDragStart = { },
                                onDragEnd = {
                                    playerManager.setBrightnessIndicator(null)
                                    playerManager.setVolumeIndicator(null)
                                },
                                onDragCancel = {
                                    playerManager.setBrightnessIndicator(null)
                                    playerManager.setVolumeIndicator(null)
                                },
                                onDrag = { change, dragAmount ->
                                    val screenWidth = size.width
                                    val isLeft = change.position.x < screenWidth / 2f
                                    val deltaRatio = -dragAmount.y / size.height.toFloat()

                                    if (isLeft) {
                                        // Brightness gesture
                                        activity?.let { act ->
                                            val lp = act.window.attributes
                                            var cur = lp.screenBrightness
                                            if (cur < 0) cur = 0.5f
                                            val target = (cur + deltaRatio * 1.5f).coerceIn(0.01f, 1.0f)
                                            lp.screenBrightness = target
                                            act.window.attributes = lp
                                            playerManager.setBrightnessIndicator(target)
                                        }
                                    } else {
                                        // Volume gesture
                                        val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                                        val curVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                                        val deltaStep = (deltaRatio * maxVol * 1.5f).toInt()
                                        val newVol = (curVol + deltaStep).coerceIn(0, maxVol)
                                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVol, 0)
                                        playerManager.setVolumeIndicator(newVol.toFloat() / maxVol.toFloat())
                                    }
                                }
                            )
                        }
                    }
            )

            // Buffering Indicator
            if (playerState.isBuffering) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = VantaPrimary,
                        modifier = Modifier.size(54.dp)
                    )
                }
            }

            // Seek Delta Indicator HUD (Visual ripple for +-10s)
            playerState.seekDeltaIndicator?.let { delta ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.75f),
                        modifier = Modifier.size(90.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = if (delta > 0) Icons.Default.Forward10 else Icons.Default.Replay10,
                                contentDescription = null,
                                tint = VantaTextPrimary,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "${if (delta > 0) "+" else ""}${delta}s",
                                color = VantaTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Brightness / Volume HUD feedback
            playerState.gestureBrightness?.let { b ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 32.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Black.copy(alpha = 0.8f),
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Icon(
                                Icons.Default.BrightnessMedium,
                                contentDescription = "Brightness",
                                tint = Color.White
                            )
                            Spacer(Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { b },
                                modifier = Modifier
                                    .height(60.dp)
                                    .width(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = VantaPrimary,
                                trackColor = Color.White.copy(alpha = 0.3f),
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "${(b * 100).toInt()}%",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            playerState.gestureVolume?.let { v ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(end = 32.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Black.copy(alpha = 0.8f),
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Icon(
                                Icons.Default.VolumeUp,
                                contentDescription = "Volume",
                                tint = Color.White
                            )
                            Spacer(Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { v },
                                modifier = Modifier
                                    .height(60.dp)
                                    .width(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = VantaPrimary,
                                trackColor = Color.White.copy(alpha = 0.3f),
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "${(v * 100).toInt()}%",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Locked indicator button (when locked, only this is shown)
            if (playerState.isLocked) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    IconButton(
                        onClick = { playerManager.toggleLock() },
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            .size(48.dp)
                            .testTag("unlock_button")
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "Unlock controls",
                            tint = VantaPrimary
                        )
                    }
                }
            }

            // Normal Overlay Controls
            AnimatedVisibility(
                visible = playerState.areControlsVisible && !playerState.isLocked,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Black.copy(alpha = 0.75f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.85f)
                                )
                            )
                        )
                ) {
                    // Top Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 20.dp)
                            .align(Alignment.TopCenter),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.testTag("player_back_button")
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = VantaTextPrimary
                            )
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                        ) {
                            Text(
                                text = title,
                                color = VantaTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (subTitle.isNotBlank()) {
                                Text(
                                    text = subTitle,
                                    color = VantaTextSecondary,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Playback Speed Button
                        IconButton(
                            onClick = { showSpeedDialog = true },
                            modifier = Modifier.testTag("speed_button")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Speed, contentDescription = "Speed", tint = VantaTextPrimary)
                                Spacer(Modifier.width(2.dp))
                                Text(
                                    text = "${playerState.playbackSpeed}x",
                                    color = VantaTextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Audio & Subtitles
                        IconButton(
                            onClick = { showTrackSheet = true },
                            modifier = Modifier.testTag("tracks_button")
                        ) {
                            Icon(
                                Icons.Default.ClosedCaption,
                                contentDescription = "Audio & Subtitles",
                                tint = if (playerState.isSubtitlesEnabled) VantaPrimary else VantaTextSecondary
                            )
                        }

                        // Lock Controls Button
                        IconButton(
                            onClick = { playerManager.toggleLock() },
                            modifier = Modifier.testTag("lock_button")
                        ) {
                            Icon(
                                Icons.Default.LockOpen,
                                contentDescription = "Lock controls",
                                tint = VantaTextPrimary
                            )
                        }
                    }

                    // Center Playback Controls
                    Row(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(28.dp)
                    ) {
                        IconButton(
                            onClick = { playerManager.seekRelative(-10_000L) },
                            modifier = Modifier
                                .size(54.dp)
                                .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                                .testTag("seek_backward_button")
                        ) {
                            Icon(
                                Icons.Default.FastRewind,
                                contentDescription = "Rewind 10 seconds",
                                tint = VantaTextPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        IconButton(
                            onClick = { playerManager.togglePlayPause() },
                            modifier = Modifier
                                .size(72.dp)
                                .background(VantaPrimary, CircleShape)
                                .testTag("play_pause_button")
                        ) {
                            Icon(
                                imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (playerState.isPlaying) "Pause" else "Play",
                                tint = Color.White,
                                modifier = Modifier.size(42.dp)
                            )
                        }

                        IconButton(
                            onClick = { playerManager.seekRelative(10_000L) },
                            modifier = Modifier
                                .size(54.dp)
                                .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                                .testTag("seek_forward_button")
                        ) {
                            Icon(
                                Icons.Default.FastForward,
                                contentDescription = "Forward 10 seconds",
                                tint = VantaTextPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    // Bottom Bar with Scrubber & Time
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 24.dp)
                            .align(Alignment.BottomCenter)
                    ) {
                        // Time Scrubber
                        var isDraggingSlider by remember { mutableStateOf(false) }
                        var sliderPosition by remember { mutableFloatStateOf(0f) }

                        val currentPos = if (isDraggingSlider) {
                            (sliderPosition * playerState.durationMs).toLong()
                        } else {
                            playerState.currentPositionMs
                        }

                        val progressRatio = if (playerState.durationMs > 0) {
                            (playerState.currentPositionMs.toFloat() / playerState.durationMs.toFloat()).coerceIn(0f, 1f)
                        } else 0f

                        Slider(
                            value = if (isDraggingSlider) sliderPosition else progressRatio,
                            onValueChange = {
                                isDraggingSlider = true
                                sliderPosition = it
                            },
                            onValueChangeFinished = {
                                val target = (sliderPosition * playerState.durationMs).toLong()
                                playerManager.seekTo(target)
                                isDraggingSlider = false
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = VantaPrimary,
                                activeTrackColor = VantaPrimary,
                                inactiveTrackColor = Color.White.copy(alpha = 0.25f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("player_scrubber")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = StorageAccessHelper.formatDuration(currentPos),
                                color = VantaTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Aspect ratio toggle
                                IconButton(
                                    onClick = { playerManager.cycleResizeMode() },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        Icons.Default.AspectRatio,
                                        contentDescription = "Aspect ratio",
                                        tint = VantaTextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(Modifier.width(8.dp))

                                Text(
                                    text = StorageAccessHelper.formatDuration(playerState.durationMs),
                                    color = VantaTextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Playback Speed Dialog
    if (showSpeedDialog) {
        val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showSpeedDialog = false },
            title = {
                Text("Playback Speed", color = VantaTextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    speeds.forEach { speed ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    playerManager.setPlaybackSpeed(speed)
                                    showSpeedDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = playerState.playbackSpeed == speed,
                                onClick = {
                                    playerManager.setPlaybackSpeed(speed)
                                    showSpeedDialog = false
                                }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "${speed}x",
                                color = if (playerState.playbackSpeed == speed) VantaPrimary else VantaTextPrimary,
                                fontWeight = if (playerState.playbackSpeed == speed) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSpeedDialog = false }) {
                    Text("Close", color = VantaPrimary)
                }
            },
            containerColor = VantaSurfaceVariant
        )
    }

    // Audio & Subtitles Bottom Sheet
    if (showTrackSheet) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { showTrackSheet = false },
            sheetState = sheetState,
            containerColor = VantaSurfaceVariant
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Audio & Subtitles",
                    color = VantaTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Audio Track",
                    color = VantaTextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                if (playerState.audioTracks.isEmpty()) {
                    Text(
                        text = "Default device audio stream",
                        color = VantaTextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                } else {
                    playerState.audioTracks.forEach { track ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    playerManager.selectAudioTrack(track)
                                }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = track.isSelected,
                                onClick = { playerManager.selectAudioTrack(track) }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "${track.label} (${track.formatDescription})",
                                color = VantaTextPrimary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Subtitles",
                    color = VantaTextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { playerManager.disableSubtitles() }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = !playerState.isSubtitlesEnabled,
                        onClick = { playerManager.disableSubtitles() }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Off", color = VantaTextPrimary, fontSize = 13.sp)
                }

                playerState.subtitleTracks.forEach { track ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { playerManager.selectSubtitleTrack(track) }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = playerState.isSubtitlesEnabled && track.isSelected,
                            onClick = { playerManager.selectSubtitleTrack(track) }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = track.label,
                            color = VantaTextPrimary,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun InaccessibleFileCard(
    title: String,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = VantaSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint = VantaPrimary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Media Reference Inaccessible",
                    color = VantaTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "\"$title\" could not be opened. The video file may have been moved, renamed, or deleted from device storage.",
                    color = VantaTextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
                Spacer(Modifier.height(20.dp))
                FilledTonalButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("inaccessible_back_button")
                ) {
                    Text("Return to Library")
                }
            }
        }
    }
}
