package com.example.ui.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.VantaAccentCyan
import com.example.ui.theme.VantaBackground
import com.example.ui.theme.VantaOutline
import com.example.ui.theme.VantaPrimary
import com.example.ui.theme.VantaSurface
import com.example.ui.theme.VantaSurfaceElevated
import com.example.ui.theme.VantaSurfaceVariant
import com.example.ui.theme.VantaTextPrimary
import com.example.ui.theme.VantaTextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

data class CineMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val isUser: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CineAiScreen(
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val messages = remember {
        mutableStateListOf(
            CineMessage(
                isUser = false,
                text = "Welcome to VANTA CineAI! I'm your cinema intelligence companion. Ask me for recommendations, series lore, episode recaps, or deep film analysis."
            )
        )
    }

    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val quickPrompts = listOf(
        "Recommend anime like Jujutsu Kaisen",
        "Explain cyberpunk film aesthetics",
        "Suggest a gripping mystery thriller",
        "Best sci-fi movies of the 21st century"
    )

    LaunchedEffect(messages.size) {
        listState.animateScrollToItem(messages.size - 1)
    }

    fun sendMessage(promptText: String) {
        if (promptText.isBlank() || isLoading) return
        val userMsg = CineMessage(isUser = true, text = promptText.trim())
        messages.add(userMsg)
        inputText = ""
        isLoading = true

        coroutineScope.launch {
            val responseText = queryGeminiOrFallback(promptText, messages)
            messages.add(CineMessage(isUser = false, text = responseText))
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VantaBackground)
            .testTag("cineai_screen")
    ) {
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = VantaAccentCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "VANTA CineAI",
                        color = VantaTextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = VantaTextPrimary)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = VantaBackground)
        )

        // Conversation List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(messages) { msg ->
                ChatBubble(message = msg)
            }

            if (isLoading) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 12.dp, top = 4.dp)
                    ) {
                        CircularProgressIndicator(
                            color = VantaAccentCyan,
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "CineAI analyzing...",
                            color = VantaTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Quick prompts suggestions
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
        ) {
            items(quickPrompts) { q ->
                AssistChip(
                    onClick = { sendMessage(q) },
                    label = { Text(q, fontSize = 11.sp, color = VantaTextPrimary) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = VantaSurfaceVariant),
                    border = AssistChipDefaults.assistChipBorder(borderColor = VantaOutline, enabled = true)
                )
            }
        }

        // Input Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("Ask CineAI anything about cinema...", fontSize = 13.sp) },
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VantaAccentCyan,
                    unfocusedBorderColor = VantaOutline,
                    focusedContainerColor = VantaSurface,
                    unfocusedContainerColor = VantaSurface,
                    focusedTextColor = VantaTextPrimary,
                    unfocusedTextColor = VantaTextPrimary
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("cineai_input_field")
            )

            Spacer(Modifier.width(10.dp))

            IconButton(
                onClick = { sendMessage(inputText) },
                enabled = inputText.isNotBlank() && !isLoading,
                modifier = Modifier
                    .size(44.dp)
                    .background(if (inputText.isNotBlank()) VantaPrimary else VantaSurfaceElevated, CircleShape)
                    .testTag("cineai_send_button")
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = if (inputText.isNotBlank()) Color.White else VantaTextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun ChatBubble(message: CineMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!message.isUser) {
            Surface(
                shape = CircleShape,
                color = VantaSurfaceElevated,
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.Top)
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = VantaAccentCyan,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(7.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = 14.dp,
                topEnd = 14.dp,
                bottomStart = if (message.isUser) 14.dp else 2.dp,
                bottomEnd = if (message.isUser) 2.dp else 14.dp
            ),
            color = if (message.isUser) VantaPrimary else VantaSurfaceVariant,
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            Text(
                text = message.text,
                color = if (message.isUser) Color.White else VantaTextPrimary,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
            )
        }

        if (message.isUser) {
            Spacer(Modifier.width(8.dp))
            Surface(
                shape = CircleShape,
                color = VantaSurfaceElevated,
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.Top)
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = VantaTextPrimary,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(7.dp)
                )
            }
        }
    }
}

private suspend fun queryGeminiOrFallback(
    prompt: String,
    history: List<CineMessage>
): String = withContext(Dispatchers.IO) {
    // Try Gemini API if key is present
    try {
        val apiKey = try {
            val field = Class.forName("com.example.BuildConfig").getField("GEMINI_API_KEY")
            field.get(null) as? String
        } catch (_: Exception) {
            System.getenv("GEMINI_API_KEY")
        }
        if (!apiKey.isNullOrBlank() && apiKey != "MY_GEMINI_API_KEY") {
            val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            conn.connectTimeout = 8000
            conn.readTimeout = 8000

            val systemInstruction = "You are VANTA CineAI, a cinematic assistant for an offline streaming platform. You provide concise, insightful movie and anime analysis, plot summaries, and recommendations."

            val jsonBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", "$systemInstruction\n\nUser Question: $prompt"))
                        })
                    })
                })
            }

            OutputStreamWriter(conn.outputStream).use { it.write(jsonBody.toString()) }

            if (conn.responseCode == 200) {
                val response = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                val root = JSONObject(response)
                val text = root.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")
                return@withContext text.trim()
            }
        }
    } catch (_: Exception) {
        // Fallback to offline cine intelligence below
    }

    // Offline Cinema Intelligence Engine (works without internet / offline)
    delay(400)
    val lower = prompt.lowercase()
    when {
        lower.contains("jujutsu") || lower.contains("anime") -> {
            "For fans of high-octane dark fantasy and anime like Jujutsu Kaisen:\n\n1. Chainsaw Man — Visceral combat, gritty urban environments, and complex devil contracts.\n2. Bleach: Thousand-Year Blood War — Grand supernatural scale with elite swordplay animation.\n3. Demon Slayer — Stunning kinetic animation by ufotable with emotional character stakes.\n4. Hell's Paradise — Lethal convicts navigating a deadly, otherworldly island."
        }
        lower.contains("cyberpunk") || lower.contains("sci-fi") -> {
            "Cyberpunk cinema relies on visual contrast: rain-drenched neon reflections set against crumbling brutalist architecture. Key staples include:\n\n• Blade Runner 2049 (Roger Deakins' iconic atmospheric lighting)\n• Ghost in the Shell (1995) (Philosophical exploration of cybernetic consciousness)\n• Akira (Sensory overload, hyper-detailed hand-drawn metropolis destruction)\n• The Matrix (Bullet-time choreography and green phosphorus tint)."
        }
        lower.contains("mystery") || lower.contains("thriller") -> {
            "Top thriller recommendations with airtight pacing:\n\n1. Prisoners (Denis Villeneuve) — Relentless psychological tension and moral ambiguity.\n2. Se7en (David Fincher) — Neo-noir masterclass with rain-soaked dread.\n3. Shutter Island — Masterful psychological suspense with an unforgettable narrative turn.\n4. Memories of Murder (Bong Joon-ho) — Riveting procedural investigating an elusive quarry."
        }
        else -> {
            "Cinema is at its finest when atmosphere, narrative economy, and sound design work in harmony. In your VANTA local library, organizing by release decade and genre lets you rediscover cinematic masterpieces with zero buffering and full local audio clarity."
        }
    }
}
