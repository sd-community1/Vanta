package com.example.util

import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import java.util.Locale
import java.util.concurrent.TimeUnit

object StorageAccessHelper {

    fun takePersistableUriPermission(context: Context, uri: Uri) {
        try {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, flags)
        } catch (_: Exception) {
            // Some content providers or external file pickers do not support persistable flags
        }
    }

    fun isUriAccessible(context: Context, uriString: String?): Boolean {
        if (uriString.isNullOrBlank()) return false
        val uri = Uri.parse(uriString)
        if (uri.scheme == "android.resource" || uri.scheme == "http" || uri.scheme == "https") {
            return true
        }
        return try {
            context.contentResolver.openInputStream(uri)?.use { true } ?: false
        } catch (_: Exception) {
            false
        }
    }

    fun getFileName(context: Context, uri: Uri): String {
        var name = ""
        try {
            if (uri.scheme == "content") {
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1) {
                            name = it.getString(nameIndex) ?: ""
                        }
                    }
                }
            }
            if (name.isEmpty()) {
                name = uri.lastPathSegment?.substringAfterLast('/') ?: "Video"
            }
        } catch (_: Exception) {
            name = uri.lastPathSegment ?: "Video"
        }
        // Remove file extension
        return name.substringBeforeLast('.', name)
    }

    data class VideoMeta(
        val durationMs: Long = 0L,
        val resolution: String = "",
        val mimeType: String = ""
    )

    fun extractVideoMetadata(context: Context, uri: Uri): VideoMeta {
        var durationMs = 0L
        var resolution = ""
        var mime = ""

        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            val dur = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            if (dur != null) {
                durationMs = dur.toLongOrNull() ?: 0L
            }
            val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull()
            val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull()
            if (width != null && height != null) {
                resolution = when {
                    height >= 2160 || width >= 3840 -> "4K UHD"
                    height >= 1080 || width >= 1920 -> "1080p FHD"
                    height >= 720 || width >= 1280 -> "720p HD"
                    else -> "${width}x${height}"
                }
            }
            mime = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE) ?: ""
            retriever.release()
        } catch (_: Exception) {
            // Safe fallback if codec/format can't be introspected synchronously
        }

        return VideoMeta(durationMs, resolution, mime)
    }

    fun formatDuration(ms: Long): String {
        if (ms <= 0) return "00:00"
        val hours = TimeUnit.MILLISECONDS.toHours(ms)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(ms) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(ms) % 60

        return if (hours > 0) {
            String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        }
    }

    fun formatDurationReadable(ms: Long): String {
        if (ms <= 0) return ""
        val hours = TimeUnit.MILLISECONDS.toHours(ms)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(ms) % 60
        return if (hours > 0) {
            "${hours}h ${minutes}m"
        } else {
            "${minutes}m"
        }
    }
}
