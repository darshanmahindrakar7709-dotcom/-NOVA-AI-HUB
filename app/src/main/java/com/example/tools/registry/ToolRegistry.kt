package com.example.tools.registry

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class ToolCategory(val displayName: String, val icon: ImageVector) {
    ALL("All Tools", Icons.Default.Apps),
    AI_CHAT("AI Chat", Icons.AutoMirrored.Filled.Chat),
    WRITING("Writing", Icons.Default.EditNote),
    STUDY("Study", Icons.Default.School),
    CODING("Coding", Icons.Default.Code),
    IMAGE("Image", Icons.Default.Image),
    VIDEO("Video", Icons.Default.Videocam),
    AUDIO("Audio", Icons.Default.GraphicEq),
    PRODUCTIVITY("Productivity", Icons.Default.CheckCircleOutline),
    UTILITIES("Utilities", Icons.Default.Build);

    val title: String get() = displayName
}

data class ToolDefinition(
    val id: String,
    val name: String,
    val description: String,
    val category: ToolCategory,
    val icon: ImageVector,
    val badge: String = "Free",
    val isOfflineReady: Boolean = true,
    val isFeatured: Boolean = false,
    val searchKeywords: List<String> = emptyList()
) {
    val title: String get() = name
}

// Alias for backwards compatibility
typealias ToolItem = ToolDefinition

object ToolRegistry {
    val allTools: List<ToolDefinition> = listOf(
        // AI Chat
        ToolDefinition(
            id = "ai_chat",
            name = "AI Chat Assistant",
            description = "Intelligent conversational assistant with code formatting, prompt templates, and local storage.",
            category = ToolCategory.AI_CHAT,
            icon = Icons.AutoMirrored.Filled.Chat,
            badge = "AI Ready",
            isOfflineReady = true,
            isFeatured = true,
            searchKeywords = listOf("chat", "ai", "conversation", "gpt", "gemini", "assistant", "question", "prompt")
        ),

        // Writing Tools
        ToolDefinition(
            id = "text_summarizer",
            name = "Text Summarizer",
            description = "Extract key takeaways and condense articles, documents, or paragraphs into bullet points.",
            category = ToolCategory.WRITING,
            icon = Icons.Default.ShortText,
            badge = "NLP",
            isOfflineReady = true,
            isFeatured = true,
            searchKeywords = listOf("summarize", "summary", "tldr", "condense", "article", "bullet points", "reading")
        ),
        ToolDefinition(
            id = "text_cleaner",
            name = "Text Cleaner & Counter",
            description = "Word/char/sentence counter, case converter, whitespace remover, and duplicate line cleaner.",
            category = ToolCategory.WRITING,
            icon = Icons.Default.FormatAlignLeft,
            badge = "Offline",
            isOfflineReady = true,
            isFeatured = false,
            searchKeywords = listOf("word count", "character count", "case converter", "uppercase", "lowercase", "cleaner", "spaces", "duplicates")
        ),
        ToolDefinition(
            id = "caption_generator",
            name = "Social Caption & Hashtags",
            description = "Generate viral captions and trending hashtags for Instagram, LinkedIn, TikTok, and X.",
            category = ToolCategory.WRITING,
            icon = Icons.Default.Tag,
            badge = "Instant",
            isOfflineReady = true,
            isFeatured = false,
            searchKeywords = listOf("caption", "hashtag", "instagram", "tiktok", "twitter", "linkedin", "social media", "post")
        ),
        ToolDefinition(
            id = "email_draft_generator",
            name = "Email Draft Studio",
            description = "Create formal, follow-up, apology, cold outreach, and job application email drafts in seconds.",
            category = ToolCategory.WRITING,
            icon = Icons.Default.Email,
            badge = "Instant",
            isOfflineReady = true,
            isFeatured = true,
            searchKeywords = listOf("email", "draft", "mail", "formal", "outreach", "follow up", "letter", "application")
        ),
        ToolDefinition(
            id = "idea_story_generator",
            name = "Story & Bio Generator",
            description = "Generate creative story ideas, plot twists, character concepts, and catchy social bios.",
            category = ToolCategory.WRITING,
            icon = Icons.Default.AutoStories,
            badge = "Instant",
            isOfflineReady = true,
            isFeatured = false,
            searchKeywords = listOf("story", "bio", "plot", "idea", "creative writing", "character", "profile", "headline")
        ),

        // Study Tools
        ToolDefinition(
            id = "notes_organizer",
            name = "Notes Organizer",
            description = "Organize rich notes with color tags, search, and local on-device database storage.",
            category = ToolCategory.STUDY,
            icon = Icons.Default.MenuBook,
            badge = "Offline",
            isOfflineReady = true,
            isFeatured = true,
            searchKeywords = listOf("notes", "study", "organizer", "memo", "notebook", "save", "markdown")
        ),
        ToolDefinition(
            id = "flashcards",
            name = "Flashcard Studio",
            description = "Create custom decks, practice with 3D flip cards, and track your study mastery score.",
            category = ToolCategory.STUDY,
            icon = Icons.Default.Style,
            badge = "Offline",
            isOfflineReady = true,
            isFeatured = true,
            searchKeywords = listOf("flashcards", "study", "deck", "anki", "memorize", "exam", "quiz")
        ),
        ToolDefinition(
            id = "quiz_creator",
            name = "Quiz & Question Creator",
            description = "Generate interactive multiple-choice tests, questions, and answers from any study text.",
            category = ToolCategory.STUDY,
            icon = Icons.Default.Quiz,
            badge = "Interactive",
            isOfflineReady = true,
            isFeatured = false,
            searchKeywords = listOf("quiz", "questions", "test", "exam", "generator", "mcq", "answers")
        ),
        ToolDefinition(
            id = "study_timer",
            name = "Study Pomodoro & Timer",
            description = "Focus blocks (25/5/15 min), customizable study timer, and session completion streak counter.",
            category = ToolCategory.STUDY,
            icon = Icons.Default.HourglassBottom,
            badge = "Offline",
            isOfflineReady = true,
            isFeatured = false,
            searchKeywords = listOf("study timer", "pomodoro", "focus", "clock", "countdown", "interval", "break")
        ),
        ToolDefinition(
            id = "study_planner",
            name = "Study Planner & Goals",
            description = "Track subjects, study schedule, daily learning goals, and exam preparation milestones.",
            category = ToolCategory.STUDY,
            icon = Icons.Default.CalendarToday,
            badge = "Offline",
            isOfflineReady = true,
            isFeatured = false,
            searchKeywords = listOf("study planner", "schedule", "goals", "timetable", "exams", "agenda")
        ),

        // Coding Tools
        ToolDefinition(
            id = "json_tool",
            name = "JSON Formatter & Validator",
            description = "Format, prettify, minify, validate syntax, and inspect JSON data with precise error pointers.",
            category = ToolCategory.CODING,
            icon = Icons.Default.DataArray,
            badge = "Offline",
            isOfflineReady = true,
            isFeatured = true,
            searchKeywords = listOf("json", "formatter", "validator", "prettify", "minify", "parser", "syntax")
        ),
        ToolDefinition(
            id = "code_beautifier",
            name = "Code Beautifier (HTML/CSS/JS)",
            description = "Format and clean HTML, CSS, JavaScript, and XML code directly in your browser/app.",
            category = ToolCategory.CODING,
            icon = Icons.Default.Code,
            badge = "Offline",
            isOfflineReady = true,
            isFeatured = false,
            searchKeywords = listOf("html", "css", "javascript", "code beautifier", "format code", "indent", "xml")
        ),
        ToolDefinition(
            id = "base64_url",
            name = "Base64 & URL Encoder/Decoder",
            description = "Encode and decode plain text to/from Base64 and URL percent-encoding safely offline.",
            category = ToolCategory.CODING,
            icon = Icons.Default.Key,
            badge = "Offline",
            isOfflineReady = true,
            isFeatured = false,
            searchKeywords = listOf("base64", "url encode", "decode", "ascii", "utf8", "encoder", "decoder")
        ),
        ToolDefinition(
            id = "color_converter",
            name = "Color Studio & Converter",
            description = "Convert HEX, RGB, HSL, HSV, and Compose Color with visual preview and live sliders.",
            category = ToolCategory.CODING,
            icon = Icons.Default.Palette,
            badge = "Offline",
            isOfflineReady = true,
            isFeatured = false,
            searchKeywords = listOf("color", "hex", "rgb", "hsl", "hsv", "palette", "picker", "converter", "css color")
        ),
        ToolDefinition(
            id = "regex_tester",
            name = "Regex Tester & Cheatsheet",
            description = "Live regular expression evaluator with match highlights, capture groups, and pattern cheatsheet.",
            category = ToolCategory.CODING,
            icon = Icons.Default.FilterAlt,
            badge = "Offline",
            isOfflineReady = true,
            isFeatured = true,
            searchKeywords = listOf("regex", "regular expression", "pattern", "matcher", "tester", "cheatsheet", "flags")
        ),

        // Image Tools
        ToolDefinition(
            id = "image_prompt",
            name = "AI Image Prompt Studio",
            description = "Build high-detail prompts with camera lenses, lighting styles, artists, and aspect ratio tags.",
            category = ToolCategory.IMAGE,
            icon = Icons.Default.Brush,
            badge = "Creative",
            isOfflineReady = true,
            isFeatured = true,
            searchKeywords = listOf("image prompt", "midjourney", "dall-e", "stable diffusion", "imagen", "prompt generator")
        ),
        ToolDefinition(
            id = "image_compressor",
            name = "Image Compressor & Resizer",
            description = "Resize dimensions and compress image file size with live before/after size estimation.",
            category = ToolCategory.IMAGE,
            icon = Icons.Default.Compress,
            badge = "Offline",
            isOfflineReady = true,
            isFeatured = false,
            searchKeywords = listOf("compress image", "resize", "shrink photo", "jpeg", "quality", "mb to kb")
        ),
        ToolDefinition(
            id = "image_converter",
            name = "Image Format Converter",
            description = "Convert local images between PNG, JPEG, and WebP formats directly on device.",
            category = ToolCategory.IMAGE,
            icon = Icons.Default.Transform,
            badge = "Offline",
            isOfflineReady = true,
            isFeatured = false,
            searchKeywords = listOf("png to jpeg", "webp converter", "image format", "photo converter", "export image")
        ),
        ToolDefinition(
            id = "image_metadata",
            name = "Image Metadata & EXIF",
            description = "Inspect image resolution, dimensions, aspect ratio, color depth, and file metrics.",
            category = ToolCategory.IMAGE,
            icon = Icons.Default.Info,
            badge = "Offline",
            isOfflineReady = true,
            isFeatured = false,
            searchKeywords = listOf("image metadata", "exif", "resolution", "dimensions", "file info", "dpi")
        ),

        // Video Tools
        ToolDefinition(
            id = "video_seo",
            name = "Video Title & SEO Studio",
            description = "Generate high-CTR click-worthy titles and search tags for YouTube, Shorts, TikTok & Reels.",
            category = ToolCategory.VIDEO,
            icon = Icons.Default.TrendingUp,
            badge = "Instant",
            isOfflineReady = true,
            isFeatured = false,
            searchKeywords = listOf("video title", "youtube", "tiktok", "shorts", "reels", "seo", "viral title", "tags")
        ),
        ToolDefinition(
            id = "video_description",
            name = "Video Description Builder",
            description = "Structured video description builder with timestamp chapters, hashtags, and social links.",
            category = ToolCategory.VIDEO,
            icon = Icons.Default.Description,
            badge = "Instant",
            isOfflineReady = true,
            isFeatured = false,
            searchKeywords = listOf("video description", "timestamps", "chapters", "youtube description", "links")
        ),
        ToolDefinition(
            id = "video_prompt",
            name = "AI Video Prompt Generator",
            description = "Craft cinematic camera movements, pans, dolly zooms, and lighting prompts for AI video models.",
            category = ToolCategory.VIDEO,
            icon = Icons.Default.MovieCreation,
            badge = "Creative",
            isOfflineReady = true,
            isFeatured = true,
            searchKeywords = listOf("video prompt", "sora", "runway", "pika", "kling", "cinematic", "camera move")
        ),
        ToolDefinition(
            id = "short_video_ideas",
            name = "Shorts & Reels Hook Generator",
            description = "Viral 3-second opening hooks, retention blueprints, and script angles for short-form video.",
            category = ToolCategory.VIDEO,
            icon = Icons.Default.SmartDisplay,
            badge = "Instant",
            isOfflineReady = true,
            isFeatured = false,
            searchKeywords = listOf("hooks", "viral hook", "shorts", "tiktok ideas", "reels", "script outline")
        ),
        ToolDefinition(
            id = "video_inspector",
            name = "Video File Inspector",
            description = "Inspect duration, resolution, frame container, bitrate, and size of local video files.",
            category = ToolCategory.VIDEO,
            icon = Icons.Default.Videocam,
            badge = "Offline",
            isOfflineReady = true,
            isFeatured = false,
            searchKeywords = listOf("video file", "duration", "bitrate", "resolution", "mp4", "inspector")
        ),

        // Audio Tools
        ToolDefinition(
            id = "podcast_titles",
            name = "Podcast & Show Title Maker",
            description = "Catchy, searchable episode names, show titles, and episode summary blueprints.",
            category = ToolCategory.AUDIO,
            icon = Icons.Default.Podcasts,
            badge = "Instant",
            isOfflineReady = true,
            isFeatured = false,
            searchKeywords = listOf("podcast", "show title", "episode", "audio title", "audio show", "spotify")
        ),
        ToolDefinition(
            id = "music_ideas",
            name = "Music & Chord Generator",
            description = "Key signatures, rich chord progressions, BPM tempo tapper, and genre music inspirations.",
            category = ToolCategory.AUDIO,
            icon = Icons.Default.MusicNote,
            badge = "Offline",
            isOfflineReady = true,
            isFeatured = false,
            searchKeywords = listOf("music", "chord progression", "key", "bpm", "tempo", "lofi", "synthwave", "scale")
        ),
        ToolDefinition(
            id = "songwriting_planner",
            name = "Song Structure Architect",
            description = "Arrangement blueprints (Intro, Verse, Pre-Chorus, Drop, Bridge, Outro) and energy pacing.",
            category = ToolCategory.AUDIO,
            icon = Icons.Default.QueueMusic,
            badge = "Offline",
            isOfflineReady = true,
            isFeatured = false,
            searchKeywords = listOf("song structure", "arrangement", "verse chorus", "songwriting", "composition")
        ),
        ToolDefinition(
            id = "voice_prompts",
            name = "Voice & Script Prompt Maker",
            description = "Cadence, pacing, inflection, tone, and director cues for voiceovers and speech synthesizers.",
            category = ToolCategory.AUDIO,
            icon = Icons.Default.RecordVoiceOver,
            badge = "Instant",
            isOfflineReady = true,
            isFeatured = false,
            searchKeywords = listOf("voiceover", "voice prompt", "speech synthesis", "narration", "script prompt", "elevenlabs")
        ),
        ToolDefinition(
            id = "audio_inspector",
            name = "Audio File Inspector",
            description = "Inspect duration, bitrate, sample rate, artist metadata, and audio container properties.",
            category = ToolCategory.AUDIO,
            icon = Icons.Default.GraphicEq,
            badge = "Offline",
            isOfflineReady = true,
            isFeatured = false,
            searchKeywords = listOf("audio inspector", "duration", "bitrate", "mp3", "flac", "wav")
        ),

        // Productivity Tools
        ToolDefinition(
            id = "todo_list",
            name = "To-Do & Tasks Manager",
            description = "Create categorized to-do lists, set priorities, track completion, and save offline in Room.",
            category = ToolCategory.PRODUCTIVITY,
            icon = Icons.Default.Checklist,
            badge = "Offline",
            isOfflineReady = true,
            isFeatured = true,
            searchKeywords = listOf("todo", "task", "checklist", "priority", "done", "goals", "productivity")
        ),
        ToolDefinition(
            id = "pomodoro_stopwatch",
            name = "Stopwatch & Precision Timer",
            description = "High-precision millisecond stopwatch with lap tracking and customizable countdown alarms.",
            category = ToolCategory.PRODUCTIVITY,
            icon = Icons.Default.Timer,
            badge = "Offline",
            isOfflineReady = true,
            isFeatured = false,
            searchKeywords = listOf("stopwatch", "timer", "countdown", "laps", "alarm", "clock", "precision")
        ),
        ToolDefinition(
            id = "calculator",
            name = "Scientific & Basic Calculator",
            description = "Arithmetic, percentages, square roots, trigonometry (sin, cos, tan), powers, and history.",
            category = ToolCategory.PRODUCTIVITY,
            icon = Icons.Default.Calculate,
            badge = "Offline",
            isOfflineReady = true,
            isFeatured = true,
            searchKeywords = listOf("calculator", "math", "scientific", "trigonometry", "addition", "multiplication", "sqrt")
        ),
        ToolDefinition(
            id = "unit_converter",
            name = "Multi-Unit Converter",
            description = "Convert Length, Weight, Temperature, Speed, Digital Storage, Volume, and Time instantly.",
            category = ToolCategory.PRODUCTIVITY,
            icon = Icons.Default.SwapHoriz,
            badge = "Offline",
            isOfflineReady = true,
            isFeatured = true,
            searchKeywords = listOf("unit converter", "length", "weight", "kg to lbs", "celsius to fahrenheit", "storage", "bytes")
        ),
        ToolDefinition(
            id = "random_generator",
            name = "Random Generator Suite",
            description = "Random number range, dice roller (D6/D20), 3D coin flipper, strong password generator.",
            category = ToolCategory.PRODUCTIVITY,
            icon = Icons.Default.Casino,
            badge = "Offline",
            isOfflineReady = true,
            isFeatured = false,
            searchKeywords = listOf("random", "dice", "coin flip", "password generator", "rng", "picker", "randomizer")
        ),

        // Utilities
        ToolDefinition(
            id = "device_info",
            name = "Device & Hardware Info",
            description = "Inspect Android OS version, SDK API level, RAM memory stats, screen DPI, and battery health.",
            category = ToolCategory.UTILITIES,
            icon = Icons.Default.Smartphone,
            badge = "Diagnostics",
            isOfflineReady = true,
            isFeatured = true,
            searchKeywords = listOf("device info", "hardware", "ram", "battery", "system", "android version", "screen")
        ),
        ToolDefinition(
            id = "qr_generator",
            name = "QR Matrix Generator",
            description = "Generate 2D QR matrix patterns for URLs, text, and contacts with 1-tap copy.",
            category = ToolCategory.UTILITIES,
            icon = Icons.Default.QrCode,
            badge = "Offline",
            isOfflineReady = true,
            isFeatured = true,
            searchKeywords = listOf("qr code", "barcode", "generator", "matrix", "url qr", "scan")
        ),
        ToolDefinition(
            id = "world_clock",
            name = "World Clock & Timezones",
            description = "Live UTC/GMT and global major city clocks (NY, SF, London, Tokyo, Sydney, Dubai).",
            category = ToolCategory.UTILITIES,
            icon = Icons.Default.Public,
            badge = "Live",
            isOfflineReady = true,
            isFeatured = false,
            searchKeywords = listOf("world clock", "timezone", "utc", "gmt", "est", "pst", "time", "clock")
        ),
        ToolDefinition(
            id = "markdown_preview",
            name = "Markdown Live Studio",
            description = "Write and live-preview markdown documents with headers, lists, code blocks, and blockquotes.",
            category = ToolCategory.UTILITIES,
            icon = Icons.Default.Code,
            badge = "Live",
            isOfflineReady = true,
            isFeatured = true,
            searchKeywords = listOf("markdown", "preview", "md editor", "formatting", "documentation")
        ),
        ToolDefinition(
            id = "uuid_hash",
            name = "UUID & Hash Generator",
            description = "Generate random UUID v4 strings, and MD5, SHA-1, and SHA-256 cryptographic hashes.",
            category = ToolCategory.UTILITIES,
            icon = Icons.Default.Fingerprint,
            badge = "Offline",
            isOfflineReady = true,
            isFeatured = false,
            searchKeywords = listOf("uuid", "guid", "hash", "md5", "sha256", "sha1", "token", "generator")
        )
    )

    fun findToolById(id: String): ToolDefinition? {
        return allTools.find { it.id == id }
    }

    fun getToolById(id: String): ToolDefinition? {
        return findToolById(id)
    }

    fun searchTools(query: String, category: ToolCategory? = null): List<ToolDefinition> {
        val filteredByCategory = if (category == null || category == ToolCategory.ALL) {
            allTools
        } else {
            allTools.filter { it.category == category }
        }

        if (query.isBlank()) return filteredByCategory

        val cleanQuery = query.trim().lowercase()
        return filteredByCategory.filter { tool ->
            tool.name.lowercase().contains(cleanQuery) ||
            tool.description.lowercase().contains(cleanQuery) ||
            tool.category.displayName.lowercase().contains(cleanQuery) ||
            tool.searchKeywords.any { it.contains(cleanQuery) }
        }
    }
}
