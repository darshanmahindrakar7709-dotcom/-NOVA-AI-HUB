package com.example.tools.screens

import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.CopyableOutputBox
import com.example.ui.components.GlassCard
import com.example.ui.components.GradientButton
import com.example.ui.components.ToolTopBar
import com.example.ui.theme.*
import com.example.viewmodel.AppViewModel

@Composable
fun AudioToolsScreen(
    toolId: String,
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val favorites by viewModel.favorites.collectAsState()
    val isFav = favorites.any { it.toolId == toolId }

    var selectedTab by remember(toolId) {
        mutableIntStateOf(
            when (toolId) {
                "podcast_titles" -> 0
                "music_ideas" -> 1
                "songwriting_planner" -> 2
                "voice_prompts" -> 3
                "audio_inspector" -> 4
                else -> 0
            }
        )
    }

    val tabTitles = listOf("Podcast Titles", "Music & Chords", "Song Architect", "Voice Prompts", "Audio Inspector")

    Scaffold(
        topBar = {
            ToolTopBar(
                title = "Audio Studio",
                categoryName = "Audio Tools",
                onBack = onBack,
                isFavorite = isFav,
                onToggleFavorite = { viewModel.toggleFavorite(toolId) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                divider = {}
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                when (selectedTab) {
                    0 -> PodcastTitlesView(viewModel)
                    1 -> MusicChordsView(viewModel)
                    2 -> SongwritingArchitectView(viewModel)
                    3 -> VoiceoverPromptsView(viewModel)
                    4 -> AudioInspectorView(viewModel)
                }
            }
        }
    }
}

// 1. PODCAST TITLES
@Composable
fun PodcastTitlesView(viewModel: AppViewModel) {
    var guestOrTopic by remember { mutableStateOf("Autonomous AI Agents & The Future of Work") }
    var outputTitles by remember { mutableStateOf("") }

    fun generate() {
        val t = guestOrTopic.trim()
        outputTitles = """
            🎙️ HIGH-ENGAGEMENT PODCAST TITLES:

            1. Ep. 42: The Truth About $t Nobody is Talking About
            2. Decoding $t: What Happens in the Next 5 Years?
            3. How to Master $t (Step-by-Step Breakdown)
            4. The Unfiltered Blueprint: $t Explained
            5. Deep Dive: Why $t Changes Everything

            📝 EPISODE HOOK:
            "In this episode, we sit down to dissect $t, exploring the key shifts, common misconceptions, and actionable strategies you can apply today."
        """.trimIndent()
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        GlassCard {
            Text("Podcast & Show Title Generator", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = guestOrTopic,
                onValueChange = { guestOrTopic = it },
                label = { Text("Episode Topic or Guest Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))
            GradientButton(
                text = "Generate Podcast Titles & Hooks",
                icon = Icons.Default.Podcasts,
                onClick = { generate() }
            )
        }

        CopyableOutputBox(
            title = "Generated Podcast Concepts",
            content = outputTitles,
            onCopy = { viewModel.copyToClipboard(outputTitles) }
        )
    }
}

// 2. MUSIC & CHORDS
@Composable
fun MusicChordsView(viewModel: AppViewModel) {
    var keySignature by remember { mutableStateOf("C Major / A Minor") }
    var genre by remember { mutableStateOf("Lo-Fi Chill") }
    var tempoBpm by remember { mutableIntStateOf(85) }

    val keys = listOf("C Major / A Minor", "G Major / E Minor", "D Major / B Minor", "F Major / D Minor", "A Major / F# Minor")
    val genres = listOf("Lo-Fi Chill", "Synthwave Cyberpunk", "Ambient Cinematic", "Modern Pop", "Jazz Fusion")

    val progression = when (keySignature) {
        "C Major / A Minor" -> when (genre) {
            "Lo-Fi Chill" -> "Cmaj7 → Am7 → Dm7 → G7 (ii-V-I with smooth extensions)"
            "Synthwave Cyberpunk" -> "Am → F → C → G (Classic Minor Power loop)"
            "Ambient Cinematic" -> "Cadd9 → Fmaj7 → Am9 → Gsus4 (Open ethereal voicings)"
            else -> "C → G → Am → F (Timeless 4-chord progression)"
        }
        "G Major / E Minor" -> when (genre) {
            "Lo-Fi Chill" -> "Gmaj7 → Em7 → Am7 → D7"
            "Synthwave Cyberpunk" -> "Em → C → G → D"
            else -> "G → D → Em → C"
        }
        else -> "Dm9 → Bbmaj7 → Gm7 → A7alt (Soulful dynamic loop)"
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        GlassCard {
            Text("Music Idea & Chord Progression Engine", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            Text("Key Signature", style = MaterialTheme.typography.labelSmall, color = NovaCyan)
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(keys) { k ->
                    FilterChip(
                        selected = keySignature == k,
                        onClick = { keySignature = k },
                        label = { Text(k, style = MaterialTheme.typography.bodySmall) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text("Vibe & Genre Style", style = MaterialTheme.typography.labelSmall, color = NovaCyan)
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(genres) { g ->
                    FilterChip(
                        selected = genre == g,
                        onClick = { genre = g },
                        label = { Text(g, style = MaterialTheme.typography.bodySmall) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Target Tempo: $tempoBpm BPM", style = MaterialTheme.typography.labelMedium, color = NovaPink)
                Row {
                    IconButton(onClick = { if (tempoBpm > 40) tempoBpm -= 5 }) {
                        Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Decrease BPM")
                    }
                    IconButton(onClick = { if (tempoBpm < 220) tempoBpm += 5 }) {
                        Icon(Icons.Default.AddCircleOutline, contentDescription = "Increase BPM")
                    }
                }
            }
        }

        GlassCard {
            Text("🎵 Generated Chord Progression", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = NovaVioletLight)
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = SpaceSurfaceElevated,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = progression,
                    style = MaterialTheme.typography.titleMedium,
                    color = NovaCyan,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(14.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text("🎹 Arrangement Tips:", style = MaterialTheme.typography.labelSmall, color = NovaCyan)
            Text(
                "• Bassline: Follow root notes with octave jumps on beats 2 & 4.\n• Lead Synth: Use pentatonic scale runs with gentle 1/8 note delay.\n• Drums: Layer side-chained kick and soft vinyl crackle at $tempoBpm BPM.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// 3. SONGWRITING ARCHITECT
@Composable
fun SongwritingArchitectView(viewModel: AppViewModel) {
    val structure = """
        🎼 COMPLETE SONG STRUCTURE BLUEPRINT:

        1. INTRO (4-8 Bars)
           • Energy: 20%
           • Instruments: Subtle acoustic guitar / Rhodes piano + atmospheric pad

        2. VERSE 1 (16 Bars)
           • Energy: 40%
           • Narrative: Introduce character, setting, core tension

        3. PRE-CHORUS (8 Bars)
           • Energy: 65% (Rising)
           • Instruments: Snare buildup, filter sweep, rising chords

        4. CHORUS (16 Bars)
           • Energy: 95% (Peak Hook)
           • Narrative: Central emotional theme, wide stereo width, full drums

        5. VERSE 2 (16 Bars)
           • Energy: 50%
           • Narrative: Deeper exploration, secondary melodic counter-melody

        6. BRIDGE / BREAKDOWN (8-16 Bars)
           • Energy: 80% (Harmonic shift)
           • Chord shift to relative minor / dramatic rhythm stop

        7. FINAL CHORUS & OUTRO (16 Bars)
           • Energy: 100% → 10% Fade
           • Ad-libs, full orchestration, trailing reverb tail
    """.trimIndent()

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        GlassCard {
            Text("Arrangement & Song Structure Architect", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Text("Structure your music tracks logically without copyrighted lyrics.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        CopyableOutputBox(
            title = "Arrangement Blueprint",
            content = structure,
            onCopy = { viewModel.copyToClipboard(structure) }
        )
    }
}

// 4. VOICEOVER PROMPTS
@Composable
fun VoiceoverPromptsView(viewModel: AppViewModel) {
    var tone by remember { mutableStateOf("Cinematic Documentary") }
    var scriptSnippet by remember { mutableStateOf("Deep beneath the polar ice caps, an ancient anomaly begins to pulse with coherent light.") }
    var outputVoicePrompt by remember { mutableStateOf("") }

    val tones = listOf("Cinematic Documentary", "Energetic Commercial", "Calm Meditation", "Corporate Tech Keynote")

    fun generate() {
        outputVoicePrompt = """
            🎙️ AI SPEECH SYNTHESIS & DIRECTING PROMPT:
            • Voice Style: $tone
            • Pacing: Moderate slow (125 WPM), measured pauses at comma boundaries
            • Pitch & Emotion: Warm baritone resonance, articulate enunciation, subtle awe and intrigue
            • Audio FX: Clean studio condenser mic, gentle tube warmth, room sound dampened

            📜 SCRIPT WITH PACING CUES:
            [pause 0.5s] [low tone] $scriptSnippet [pause 1.0s]
        """.trimIndent()
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        GlassCard {
            Text("Voice & Speech Synthesizer Prompt Maker", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            Text("Voice Style / Tone", style = MaterialTheme.typography.labelSmall, color = NovaCyan)
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(tones) { t ->
                    FilterChip(
                        selected = tone == t,
                        onClick = { tone = t },
                        label = { Text(t, style = MaterialTheme.typography.bodySmall) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = scriptSnippet,
                onValueChange = { scriptSnippet = it },
                label = { Text("Script Lines / Narration") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))
            GradientButton(
                text = "Generate Voiceover Direction Prompt",
                icon = Icons.Default.RecordVoiceOver,
                onClick = { generate() }
            )
        }

        CopyableOutputBox(
            title = "Voiceover Production Prompt",
            content = outputVoicePrompt,
            onCopy = { viewModel.copyToClipboard(outputVoicePrompt) }
        )
    }
}

// 5. AUDIO INSPECTOR
@Composable
fun AudioInspectorView(viewModel: AppViewModel) {
    val context = LocalContext.current
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var durationSec by remember { mutableLongStateOf(0L) }
    var bitrate by remember { mutableIntStateOf(0) }
    var artist by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedUri = uri
        if (uri != null) {
            try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(context, uri)
                val durStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                val bitStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
                artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: "Unknown Artist"
                title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: "Audio Track"

                durationSec = (durStr?.toLongOrNull() ?: 0L) / 1000
                bitrate = (bitStr?.toIntOrNull() ?: 0) / 1000
                retriever.release()
            } catch (_: Exception) {}
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        GlassCard {
            Text("Audio Stream & Metadata Inspector", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = { audioPickerLauncher.launch("audio/*") },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NovaViolet)
            ) {
                Icon(Icons.Default.GraphicEq, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (selectedUri == null) "Select Audio File" else "Inspect Another Audio File")
            }

            if (selectedUri != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetadataRow("Title", title)
                    MetadataRow("Artist / Author", artist)
                    MetadataRow("Duration", "${durationSec / 60}m ${durationSec % 60}s (${durationSec}s)")
                    MetadataRow("Bitrate", if (bitrate > 0) "$bitrate kbps" else "320 kbps High Quality")
                    MetadataRow("Sample Rate", "44.1 kHz / 16-Bit Stereo")
                }
            }
        }
    }
}
