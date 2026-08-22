package com.example.tools.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.CopyableOutputBox
import com.example.ui.components.GlassCard
import com.example.ui.components.GradientButton
import com.example.ui.components.ToolTopBar
import com.example.ui.theme.*
import com.example.viewmodel.AppViewModel

@Composable
fun WritingToolsScreen(
    toolId: String,
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val favorites by viewModel.favorites.collectAsState()
    val isFav = favorites.any { it.toolId == toolId }

    var selectedTab by remember(toolId) {
        mutableIntStateOf(
            when (toolId) {
                "text_summarizer" -> 0
                "text_cleaner" -> 1
                "caption_generator" -> 2
                "email_draft_generator" -> 3
                "idea_story_generator" -> 4
                else -> 0
            }
        )
    }

    val tabTitles = listOf("Summarizer", "Cleaner & Stats", "Captions", "Email Studio", "Story & Bios")

    Scaffold(
        topBar = {
            ToolTopBar(
                title = "Writing Studio",
                categoryName = "Writing Tools",
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
                    0 -> TextSummarizerTool(viewModel)
                    1 -> TextCleanerTool(viewModel)
                    2 -> CaptionHashtagTool(viewModel)
                    3 -> EmailStudioTool(viewModel)
                    4 -> StoryBioTool(viewModel)
                }
            }
        }
    }
}

@Composable
fun TextSummarizerTool(viewModel: AppViewModel) {
    var inputText by remember {
        mutableStateOf(
            "Artificial intelligence is transforming industries across the globe. By leveraging machine learning models and high-performance computing, software can now process natural language, identify complex visual patterns, and automate repetitive tasks. However, ensuring privacy, algorithmic fairness, and data security remains critical as modern tools become increasingly ubiquitous in daily workflows."
        )
    }
    var summaryType by remember { mutableStateOf("Key Bullets") }
    var outputText by remember { mutableStateOf("") }

    val summaryTypes = listOf("Key Bullets", "Executive TL;DR", "Action Points", "1-Sentence")

    fun generateSummary() {
        if (inputText.isBlank()) {
            outputText = ""
            return
        }
        val sentences = inputText.split(Regex("(?<=[.!?])\\s+")).filter { it.isNotBlank() }
        outputText = when (summaryType) {
            "Key Bullets" -> {
                sentences.take(4).mapIndexed { idx, s -> "• ${s.trim()}" }.joinToString("\n\n")
            }
            "Executive TL;DR" -> {
                "📌 TL;DR: " + (sentences.firstOrNull() ?: inputText.take(120)) + "\n\n💡 Key Insight: Prioritize execution while maintaining privacy and quality standards."
            }
            "Action Points" -> {
                sentences.take(3).mapIndexed { idx, s -> "Step ${idx + 1}: Implement core aspects of '${s.take(40)}...'" }.joinToString("\n")
            }
            "1-Sentence" -> {
                sentences.firstOrNull() ?: inputText
            }
            else -> sentences.joinToString("\n")
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        GlassCard {
            Text("Source Text", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                placeholder = { Text("Paste long article, essay, or notes here...") },
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))
            Text("Summary Mode", style = MaterialTheme.typography.labelMedium, color = NovaCyan)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                summaryTypes.forEach { type ->
                    FilterChip(
                        selected = summaryType == type,
                        onClick = { summaryType = type },
                        label = { Text(type, style = MaterialTheme.typography.bodySmall) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            GradientButton(
                text = "Summarize Text",
                icon = Icons.Default.AutoAwesome,
                onClick = { generateSummary() }
            )
        }

        CopyableOutputBox(
            title = "Summary Result",
            content = outputText,
            onCopy = { viewModel.copyToClipboard(outputText) }
        )

        if (outputText.isNotBlank()) {
            Button(
                onClick = {
                    viewModel.saveNote(
                        title = "Summary: ${inputText.take(25)}...",
                        content = outputText,
                        tag = "Summary",
                        colorHex = "#06B6D4"
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SpaceSurfaceElevated)
            ) {
                Icon(Icons.Default.BookmarkAdd, contentDescription = null, tint = NovaCyan)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Summary to Notes", color = Color.White)
            }
        }
    }
}

@Composable
fun TextCleanerTool(viewModel: AppViewModel) {
    var text by remember { mutableStateOf("NOVA   AI   HUB provides 100% free, offline tools for developers and creators.") }

    val wordsCount = remember(text) { if (text.isBlank()) 0 else text.trim().split(Regex("\\s+")).size }
    val charsCount = remember(text) { text.length }
    val charsNoSpaces = remember(text) { text.replace(" ", "").length }
    val sentencesCount = remember(text) { if (text.isBlank()) 0 else text.split(Regex("[.!?]+")).filter { it.isNotBlank() }.size }
    val readingTimeMin = remember(wordsCount) { maxOf(1, (wordsCount / 200)) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Stats Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard("Words", "$wordsCount", Modifier.weight(1f), NovaViolet)
            StatCard("Characters", "$charsCount", Modifier.weight(1f), NovaCyan)
            StatCard("Sentences", "$sentencesCount", Modifier.weight(1f), NovaPink)
            StatCard("Read Time", "${readingTimeMin}m", Modifier.weight(1f), NovaAmber)
        }

        GlassCard {
            Text("Input & Formatter Canvas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("Transformations", style = MaterialTheme.typography.labelMedium, color = NovaCyan)
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(
                        onClick = { text = text.uppercase() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) { Text("UPPERCASE", style = MaterialTheme.typography.labelSmall) }

                    FilledTonalButton(
                        onClick = { text = text.lowercase() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) { Text("lowercase", style = MaterialTheme.typography.labelSmall) }

                    FilledTonalButton(
                        onClick = {
                            text = text.split(" ").joinToString(" ") { word ->
                                word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) { Text("Title Case", style = MaterialTheme.typography.labelSmall) }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(
                        onClick = { text = text.replace(Regex("[ \\t]+"), " ").trim() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) { Text("Trim Spaces", style = MaterialTheme.typography.labelSmall) }

                    FilledTonalButton(
                        onClick = {
                            text = text.lines().distinct().joinToString("\n")
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) { Text("Deduplicate Lines", style = MaterialTheme.typography.labelSmall) }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(
                        onClick = {
                            val clean = text.replace(Regex("[^a-zA-Z0-9 ]"), " ")
                            val words = clean.split(Regex("\\s+")).filter { it.isNotBlank() }
                            text = words.mapIndexed { idx, w ->
                                if (idx == 0) w.lowercase() else w.lowercase().replaceFirstChar { it.uppercase() }
                            }.joinToString("")
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) { Text("camelCase", style = MaterialTheme.typography.labelSmall) }

                    FilledTonalButton(
                        onClick = {
                            text = text.trim().lowercase().replace(Regex("[^a-zA-Z0-9]+"), "_")
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) { Text("snake_case", style = MaterialTheme.typography.labelSmall) }

                    FilledTonalButton(
                        onClick = {
                            text = text.trim().lowercase().replace(Regex("[^a-zA-Z0-9]+"), "-")
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) { Text("kebab-case", style = MaterialTheme.typography.labelSmall) }
                }
            }
        }

        CopyableOutputBox(
            title = "Processed Output",
            content = text,
            onCopy = { viewModel.copyToClipboard(text) }
        )
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier, color: Color) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.titleLarge, color = color, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun CaptionHashtagTool(viewModel: AppViewModel) {
    var topic by remember { mutableStateOf("New mobile app launch with free offline AI tools") }
    var platform by remember { mutableStateOf("Instagram") }
    var tone by remember { mutableStateOf("Engaging & Viral") }
    var generatedCaption by remember { mutableStateOf("") }

    val platforms = listOf("Instagram", "LinkedIn", "TikTok", "X / Twitter")
    val tones = listOf("Engaging & Viral", "Professional", "Aesthetic & Minimal", "Humorous")

    fun generate() {
        if (topic.isBlank()) return
        val cleanTopic = topic.trim()
        generatedCaption = when (platform) {
            "Instagram" ->
                "✨ Game-changing update: $cleanTopic!\n\nNo accounts, no paywalls, just pure instant productivity right at your fingertips 🚀\n\nDrop a 🔥 below if you want the link!\n\n#ProductivityTools #MobileDev #AITools #TechLife #FreeWorkspace #BuildInPublic #Innovation"
            "LinkedIn" ->
                "Excited to share our latest milestone: $cleanTopic.\n\nKey takeaways:\n1. Zero friction guest access\n2. High-performance offline computing\n3. Empowering developers and creators everywhere\n\nHow do you streamline your daily workflows? Would love to hear your insights in the comments below.\n\n#Technology #SoftwareEngineering #Productivity #AI #Innovation"
            "TikTok" ->
                "Stop paying for tools when you can get $cleanTopic for FREE 🤯 Save this video before you lose it! 📲 Link in bio.\n\n#techhack #freetools #studytok #aitools #lifehack #productivitytips"
            else ->
                "Just launched: $cleanTopic ⚡\n\n100% free • No signup required • Works offline\n\nTry it now and let me know your thoughts 👇 #Tech #Coding #AI"
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        GlassCard {
            Text("Topic / Concept", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = topic,
                onValueChange = { topic = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("What is your post about?") },
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))
            Text("Target Platform", style = MaterialTheme.typography.labelMedium, color = NovaCyan)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                platforms.forEach { p ->
                    FilterChip(
                        selected = platform == p,
                        onClick = { platform = p },
                        label = { Text(p, style = MaterialTheme.typography.bodySmall) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            GradientButton(
                text = "Generate Caption & Hashtags",
                icon = Icons.Default.Tag,
                onClick = { generate() }
            )
        }

        CopyableOutputBox(
            title = "Generated Social Post",
            content = generatedCaption,
            onCopy = { viewModel.copyToClipboard(generatedCaption) }
        )
    }
}

@Composable
fun EmailStudioTool(viewModel: AppViewModel) {
    var emailType by remember { mutableStateOf("Follow-up") }
    var recipient by remember { mutableStateOf("Alex") }
    var context by remember { mutableStateOf("Following up on yesterday's discussion regarding project milestones and deliverables.") }
    var outputEmail by remember { mutableStateOf("") }

    val emailTypes = listOf("Follow-up", "Cold Outreach", "Formal Inquiry", "Meeting Request", "Thank You")

    fun generate() {
        val name = recipient.ifBlank { "there" }
        outputEmail = when (emailType) {
            "Follow-up" ->
                "Subject: Following Up: Our Recent Discussion\n\nHi $name,\n\nI hope you're having a productive week.\n\nI am writing to briefly follow up on our previous conversation regarding: $context.\n\nPlease let me know if you have had a chance to review the details, or if there is any additional information I can provide.\n\nLooking forward to hearing from you.\n\nBest regards,\n[Your Name]"
            "Cold Outreach" ->
                "Subject: Quick Question regarding your workflow / collaboration\n\nHi $name,\n\nI noticed your recent work and wanted to reach out directly.\n\n$context\n\nI would love to explore if there might be potential synergies. Do you have 10 minutes next Tuesday for a quick introductory chat?\n\nThank you for your time,\n[Your Name]"
            "Formal Inquiry" ->
                "Subject: Inquiry Regarding $context\n\nDear $name,\n\nI am writing to respectfully request further information concerning: $context.\n\nCould you please guide me on the appropriate documentation and next steps required to proceed?\n\nThank you very much for your time and assistance.\n\nSincerely,\n[Your Name]"
            "Meeting Request" ->
                "Subject: Proposed Meeting: $context\n\nHi $name,\n\nI hope all is well with you.\n\nI'd like to schedule a brief 20-minute sync to discuss: $context.\n\nAre you available on Thursday morning or Friday afternoon? Please let me know what times suit your calendar best.\n\nWarm regards,\n[Your Name]"
            else ->
                "Subject: Thank You for Your Time\n\nDear $name,\n\nThank you so much for speaking with me today regarding: $context.\n\nI truly appreciate your valuable time and helpful insights. Please feel free to reach out if any questions arise.\n\nBest regards,\n[Your Name]"
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        GlassCard {
            Text("Email Configuration", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            Text("Email Purpose", style = MaterialTheme.typography.labelMedium, color = NovaCyan)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                emailTypes.take(3).forEach { t ->
                    FilterChip(
                        selected = emailType == t,
                        onClick = { emailType = t },
                        label = { Text(t, style = MaterialTheme.typography.bodySmall) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = recipient,
                onValueChange = { recipient = it },
                label = { Text("Recipient Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = context,
                onValueChange = { context = it },
                label = { Text("Context & Key Points") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 90.dp),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
            GradientButton(
                text = "Draft Professional Email",
                icon = Icons.Default.Email,
                onClick = { generate() }
            )
        }

        CopyableOutputBox(
            title = "Generated Email Draft",
            content = outputEmail,
            onCopy = { viewModel.copyToClipboard(outputEmail) }
        )
    }
}

@Composable
fun StoryBioTool(viewModel: AppViewModel) {
    var mode by remember { mutableStateOf("Story Hook") }
    var genreOrRole by remember { mutableStateOf("Cyberpunk / Sci-Fi") }
    var outputText by remember { mutableStateOf("") }

    val modes = listOf("Story Hook", "Character Bio", "Profile Bio", "Catchy Titles")

    fun generate() {
        outputText = when (mode) {
            "Story Hook" ->
                "⚡ **Plot Hook:**\nIn a neon-drenched metropolis powered by sentient quantum circuits, an outlaw archivist discovers a hidden memory partition belonging to the city's founder. It reveals the colony never actually reached Earth 2.0—they are still adrift in deep cryo-sleep, and the entire cyber-city is a shared neural simulation about to reboot in 48 hours."
            "Character Bio" ->
                "👤 **Character Profile: Dr. Ethan Vance**\n• **Role**: Rogue Quantum Neurologist\n• **Motivation**: Reverse the memory wipe that erased his sister's consciousness.\n• **Flaw**: Relies heavily on forbidden neuro-stimulants to sustain 72-hour coding sprints.\n• **Signature Item**: A cracked brass pocket watch containing an encrypted cold-storage key."
            "Profile Bio" ->
                "✨ **Option 1 (Developer / Tech):**\n🚀 Building high-performance, offline-first digital experiences | Open Source Enthusiast 💻 | Exploring AI & Distributed Systems 🌐\n\n🎨 **Option 2 (Creator / Minimalist):**\nDesigning systems that give you your time back. 💡 Zero fluff. Maximum impact. | Writer & Creator ✍️"
            else ->
                "🔥 **Top 5 High-Impact Titles:**\n1. The Zero-Friction Future: Why Simplicity Wins Every Time\n2. How to Build Completely Offline-First Mobile Architectures\n3. The Architect's Dilemma: Speed vs Maintainability in Modern Software\n4. 7 Productivity Hacks That Don't Require Subscriptions\n5. Mastering Focus in an Age of Hyper-Stimulation"
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        GlassCard {
            Text("Story & Bio Generator", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                modes.forEach { m ->
                    FilterChip(
                        selected = mode == m,
                        onClick = { mode = m },
                        label = { Text(m, style = MaterialTheme.typography.bodySmall) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = genreOrRole,
                onValueChange = { genreOrRole = it },
                label = { Text("Genre / Role / Vibe") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
            GradientButton(
                text = "Generate Creative Content",
                icon = Icons.Default.AutoStories,
                onClick = { generate() }
            )
        }

        CopyableOutputBox(
            title = "Generated Output",
            content = outputText,
            onCopy = { viewModel.copyToClipboard(outputText) }
        )
    }
}
