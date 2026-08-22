package com.example.tools.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FlashcardDeckEntity
import com.example.data.FlashcardEntity
import com.example.data.NoteEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.AppViewModel
import kotlinx.coroutines.delay

@Composable
fun StudyToolsScreen(
    toolId: String,
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val favorites by viewModel.favorites.collectAsState()
    val isFav = favorites.any { it.toolId == toolId }

    var selectedTab by remember(toolId) {
        mutableIntStateOf(
            when (toolId) {
                "notes_organizer" -> 0
                "flashcards" -> 1
                "quiz_creator" -> 2
                "study_timer" -> 3
                "study_planner" -> 4
                else -> 0
            }
        )
    }

    val tabTitles = listOf("Notes", "Flashcards", "Quiz Maker", "Focus Timer", "Planner")

    Scaffold(
        topBar = {
            ToolTopBar(
                title = "Study Hub",
                categoryName = "Study Tools",
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
                    .padding(16.dp)
            ) {
                when (selectedTab) {
                    0 -> NotesManagerView(viewModel)
                    1 -> FlashcardStudioView(viewModel)
                    2 -> QuizMakerView(viewModel)
                    3 -> StudyTimerView(viewModel)
                    4 -> StudyPlannerView(viewModel)
                }
            }
        }
    }
}

// 1. NOTES ORGANIZER
@Composable
fun NotesManagerView(viewModel: AppViewModel) {
    val notes by viewModel.allNotes.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var noteToEdit by remember { mutableStateOf<NoteEntity?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedTag by remember { mutableStateOf("All") }

    val tags = listOf("All", "Guide", "Study", "General", "Ideas", "Code")

    val filteredNotes = notes.filter { note ->
        (selectedTag == "All" || note.tag.equals(selectedTag, ignoreCase = true)) &&
        (searchQuery.isBlank() || note.title.contains(searchQuery, true) || note.content.contains(searchQuery, true))
    }

    if (showAddDialog || noteToEdit != null) {
        NoteEditDialog(
            existingNote = noteToEdit,
            onDismiss = {
                showAddDialog = false
                noteToEdit = null
            },
            onSave = { title, content, tag, color ->
                viewModel.saveNote(
                    id = noteToEdit?.id ?: 0,
                    title = title,
                    content = content,
                    tag = tag,
                    colorHex = color
                )
                showAddDialog = false
                noteToEdit = null
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search notes...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = NovaViolet,
                contentColor = Color.White,
                modifier = Modifier.size(52.dp).testTag("add_note_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Note")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(tags) { tag ->
                FilterChip(
                    selected = selectedTag == tag,
                    onClick = { selectedTag = tag },
                    label = { Text(tag, style = MaterialTheme.typography.bodySmall) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredNotes.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.MenuBook,
                title = "No notes found",
                subtitle = "Create your first study note or change your search filters."
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredNotes) { note ->
                    GlassCard(
                        onClick = { noteToEdit = note },
                        borderColor = Color(android.graphics.Color.parseColor(note.colorHex.ifBlank { "#8B5CF6" })).copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = note.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            SuggestionChip(
                                onClick = {},
                                label = { Text(note.tag, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = note.content,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 3,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(onClick = { viewModel.copyToClipboard(note.content) }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy note", modifier = Modifier.size(16.dp))
                            }
                            IconButton(onClick = { viewModel.deleteNote(note) }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete note", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NoteEditDialog(
    existingNote: NoteEntity?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf(existingNote?.title ?: "") }
    var content by remember { mutableStateOf(existingNote?.content ?: "") }
    var tag by remember { mutableStateOf(existingNote?.tag ?: "Study") }
    var colorHex by remember { mutableStateOf(existingNote?.colorHex ?: "#8B5CF6") }

    val colors = listOf("#8B5CF6", "#06B6D4", "#EC4899", "#10B981", "#F59E0B")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existingNote == null) "New Note" else "Edit Note") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Note Title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Content (supports Markdown)") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = tag,
                    onValueChange = { tag = it },
                    label = { Text("Tag / Category") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    colors.forEach { c ->
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(Color(android.graphics.Color.parseColor(c)), CircleShape)
                                .border(
                                    width = if (colorHex == c) 2.dp else 0.dp,
                                    color = Color.White,
                                    shape = CircleShape
                                )
                                .clickable { colorHex = c }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(title, content, tag, colorHex) },
                enabled = title.isNotBlank() || content.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// 2. FLASHCARD STUDIO
@Composable
fun FlashcardStudioView(viewModel: AppViewModel) {
    val decks by viewModel.allDecks.collectAsState()
    var selectedDeck by remember { mutableStateOf<FlashcardDeckEntity?>(null) }
    var showCreateDeckDialog by remember { mutableStateOf(false) }

    if (showCreateDeckDialog) {
        var deckTitle by remember { mutableStateOf("") }
        var deckDesc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreateDeckDialog = false },
            title = { Text("Create Flashcard Deck") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = deckTitle,
                        onValueChange = { deckTitle = it },
                        label = { Text("Deck Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = deckDesc,
                        onValueChange = { deckDesc = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.createDeck(deckTitle, deckDesc)
                        showCreateDeckDialog = false
                    },
                    enabled = deckTitle.isNotBlank()
                ) {
                    Text("Create Deck")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDeckDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (selectedDeck != null) {
        DeckPracticeScreen(
            deck = selectedDeck!!,
            viewModel = viewModel,
            onBackToDecks = { selectedDeck = null }
        )
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Study Decks", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Button(
                    onClick = { showCreateDeckDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NovaViolet)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Deck")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (decks.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.Style,
                    title = "No flashcard decks yet",
                    subtitle = "Create decks to memorize vocabulary, interview concepts, or exam questions."
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(decks) { deck ->
                        GlassCard(
                            onClick = { selectedDeck = deck }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(deck.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    if (deck.description.isNotBlank()) {
                                        Text(deck.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Row {
                                    FilledTonalButton(onClick = { selectedDeck = deck }) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Study")
                                    }
                                    IconButton(onClick = { viewModel.deleteDeck(deck) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeckPracticeScreen(
    deck: FlashcardDeckEntity,
    viewModel: AppViewModel,
    onBackToDecks: () -> Unit
) {
    val cardsFlow = remember(deck.id) { viewModel.getCardsForDeck(deck.id) }
    val cards by cardsFlow.collectAsState(initial = emptyList())

    var currentIndex by remember { mutableIntStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }
    var score by remember { mutableIntStateOf(0) }
    var showAddCardDialog by remember { mutableStateOf(false) }

    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 350),
        label = "card_flip"
    )

    if (showAddCardDialog) {
        var question by remember { mutableStateOf("") }
        var answer by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddCardDialog = false },
            title = { Text("Add Flashcard") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = question,
                        onValueChange = { question = it },
                        label = { Text("Question / Front") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = answer,
                        onValueChange = { answer = it },
                        label = { Text("Answer / Back") },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 90.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addCard(deck.id, question, answer)
                        showAddCardDialog = false
                    },
                    enabled = question.isNotBlank() && answer.isNotBlank()
                ) {
                    Text("Add Card")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCardDialog = false }) { Text("Cancel") }
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackToDecks) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(deck.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            IconButton(onClick = { showAddCardDialog = true }) {
                Icon(Icons.Default.AddCircleOutline, contentDescription = "Add Card", tint = NovaCyan)
            }
        }

        if (cards.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.Style,
                title = "No cards in this deck",
                subtitle = "Tap the + icon above to add your first question and answer card."
            )
        } else {
            val card = cards.getOrNull(currentIndex) ?: cards.first()
            val total = cards.size
            val progress = (currentIndex + 1).toFloat() / total

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = NovaViolet
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Card ${currentIndex + 1} of $total", style = MaterialTheme.typography.labelMedium, color = NovaCyan)
                Text("Tap card to flip", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // 3D Flip Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .graphicsLayer {
                        rotationY = rotation
                        cameraDistance = 12f * density
                    }
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isFlipped) NovaVioletDark else SpaceSurfaceElevated)
                    .border(2.dp, if (isFlipped) NovaPink else NovaViolet, RoundedCornerShape(20.dp))
                    .clickable { isFlipped = !isFlipped }
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                if (rotation <= 90f) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("QUESTION", style = MaterialTheme.typography.labelSmall, color = NovaCyan, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = card.question,
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.graphicsLayer { rotationY = 180f },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("ANSWER", style = MaterialTheme.typography.labelSmall, color = NovaPinkLight, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = card.answer,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        isFlipped = false
                        if (currentIndex < cards.size - 1) currentIndex++ else currentIndex = 0
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Needs Review")
                }

                Button(
                    onClick = {
                        score++
                        isFlipped = false
                        if (currentIndex < cards.size - 1) currentIndex++ else currentIndex = 0
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NovaEmerald)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Got It Right!")
                }
            }
        }
    }
}

// 3. QUIZ CREATOR
@Composable
fun QuizMakerView(viewModel: AppViewModel) {
    var topic by remember { mutableStateOf("Photosynthesis and Plant Biology") }
    var activeQuizQuestion by remember { mutableIntStateOf(0) }
    var selectedAnswer by remember { mutableStateOf<Int?>(null) }
    var quizGenerated by remember { mutableStateOf(false) }

    data class QuizQuestion(val question: String, val options: List<String>, val correctIdx: Int, val explanation: String)

    val sampleQuestions = listOf(
        QuizQuestion(
            question = "Which organelle is primarily responsible for photosynthesis in plant cells?",
            options = listOf("Mitochondria", "Chloroplast", "Endoplasmic Reticulum", "Golgi Apparatus"),
            correctIdx = 1,
            explanation = "Chloroplasts contain chlorophyll pigments that absorb solar photons to synthesize glucose."
        ),
        QuizQuestion(
            question = "What gas is released as a byproduct during the light reactions of photosynthesis?",
            options = listOf("Carbon Dioxide", "Nitrogen", "Oxygen", "Methane"),
            correctIdx = 2,
            explanation = "Water molecules (H2O) are split during photolysis, releasing O2 gas into the atmosphere."
        ),
        QuizQuestion(
            question = "What is the primary chemical product synthesized in the Calvin Cycle?",
            options = listOf("Glyceraldehyde 3-Phosphate (G3P)", "Lactic Acid", "Adenosine Monophosphate", "Cellulose"),
            correctIdx = 0,
            explanation = "The Calvin Cycle fixes atmospheric CO2 into G3P 3-carbon sugar precursors."
        )
    )

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        GlassCard {
            Text("AI Quiz & Question Generator", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = topic,
                onValueChange = { topic = it },
                label = { Text("Study Topic or Paste Notes") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            GradientButton(
                text = "Generate Interactive Quiz",
                icon = Icons.Default.Quiz,
                onClick = {
                    quizGenerated = true
                    activeQuizQuestion = 0
                    selectedAnswer = null
                }
            )
        }

        if (quizGenerated) {
            val q = sampleQuestions[activeQuizQuestion]

            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Question ${activeQuizQuestion + 1} of ${sampleQuestions.size}", style = MaterialTheme.typography.labelMedium, color = NovaCyan)
                    Text("Topic: $topic", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(q.question, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(12.dp))

                q.options.forEachIndexed { idx, opt ->
                    val isSelected = selectedAnswer == idx
                    val isCorrect = idx == q.correctIdx
                    val bgColor = when {
                        selectedAnswer == null -> MaterialTheme.colorScheme.surfaceVariant
                        isSelected && isCorrect -> NovaEmerald.copy(alpha = 0.25f)
                        isSelected && !isCorrect -> MaterialTheme.colorScheme.error.copy(alpha = 0.25f)
                        isCorrect -> NovaEmerald.copy(alpha = 0.25f)
                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(enabled = selectedAnswer == null) {
                                selectedAnswer = idx
                            },
                        color = bgColor,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) NovaViolet else Color.Transparent
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${('A' + idx)}. $opt",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.weight(1f)
                            )
                            if (selectedAnswer != null) {
                                if (isCorrect) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NovaEmerald)
                                } else if (isSelected) {
                                    Icon(Icons.Default.Cancel, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }

                if (selectedAnswer != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = SpaceSurface,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("💡 Explanation:", style = MaterialTheme.typography.labelSmall, color = NovaCyan)
                            Text(q.explanation, style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            if (activeQuizQuestion < sampleQuestions.size - 1) {
                                activeQuizQuestion++
                                selectedAnswer = null
                            } else {
                                activeQuizQuestion = 0
                                selectedAnswer = null
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (activeQuizQuestion < sampleQuestions.size - 1) "Next Question" else "Restart Quiz")
                    }
                }
            }
        }
    }
}

// 4. STUDY TIMER & POMODORO
@Composable
fun StudyTimerView(viewModel: AppViewModel) {
    var mode by remember { mutableStateOf("Focus (25m)") }
    var totalSeconds by remember { mutableIntStateOf(25 * 60) }
    var secondsLeft by remember { mutableIntStateOf(25 * 60) }
    var isRunning by remember { mutableStateOf(false) }
    var completedSessions by remember { mutableIntStateOf(3) }

    LaunchedEffect(isRunning, secondsLeft) {
        if (isRunning && secondsLeft > 0) {
            delay(1000L)
            secondsLeft--
        } else if (isRunning && secondsLeft == 0) {
            isRunning = false
            completedSessions++
        }
    }

    val minutes = secondsLeft / 60
    val seconds = secondsLeft % 60
    val progress = 1f - (secondsLeft.toFloat() / totalSeconds.toFloat())

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = mode == "Focus (25m)",
                onClick = {
                    mode = "Focus (25m)"
                    totalSeconds = 25 * 60
                    secondsLeft = 25 * 60
                    isRunning = false
                },
                label = { Text("Focus (25m)") }
            )
            FilterChip(
                selected = mode == "Short Break (5m)",
                onClick = {
                    mode = "Short Break (5m)"
                    totalSeconds = 5 * 60
                    secondsLeft = 5 * 60
                    isRunning = false
                },
                label = { Text("Short Break (5m)") }
            )
            FilterChip(
                selected = mode == "Long Break (15m)",
                onClick = {
                    mode = "Long Break (15m)"
                    totalSeconds = 15 * 60
                    secondsLeft = 15 * 60
                    isRunning = false
                },
                label = { Text("Long Break (15m)") }
            )
        }

        // Circular Timer Visual
        Box(
            modifier = Modifier.size(240.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.size(230.dp),
                strokeWidth = 10.dp,
                color = NovaViolet,
                trackColor = SpaceSurfaceElevated
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = String.format("%02d:%02d", minutes, seconds),
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
                Text(
                    text = if (isRunning) "Studying..." else "Paused",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isRunning) NovaEmerald else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Controls
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            FloatingActionButton(
                onClick = { isRunning = !isRunning },
                containerColor = if (isRunning) NovaAmber else NovaViolet,
                contentColor = Color.White,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Toggle Timer",
                    modifier = Modifier.size(32.dp)
                )
            }

            FloatingActionButton(
                onClick = {
                    isRunning = false
                    secondsLeft = totalSeconds
                },
                containerColor = SpaceSurfaceElevated,
                contentColor = Color.White,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Reset Timer", modifier = Modifier.size(28.dp))
            }
        }

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Today's Study Streak", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("$completedSessions completed focus intervals", style = MaterialTheme.typography.bodySmall, color = NovaCyan)
                }
                Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = NovaAmber, modifier = Modifier.size(36.dp))
            }
        }
    }
}

// 5. STUDY PLANNER
@Composable
fun StudyPlannerView(viewModel: AppViewModel) {
    var subjects by remember {
        mutableStateOf(
            listOf(
                "Data Structures & Algorithms" to true,
                "Mobile UI & Jetpack Compose Architecture" to true,
                "Database Normalization & Room ORM" to false,
                "Operating Systems: Memory Management" to false
            )
        )
    }
    var newSubject by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        GlassCard {
            Text("Weekly Study Milestones", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedTextField(
                    value = newSubject,
                    onValueChange = { newSubject = it },
                    placeholder = { Text("Add subject goal...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (newSubject.isNotBlank()) {
                            subjects = subjects + (newSubject.trim() to false)
                            newSubject = ""
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NovaViolet)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                }
            }
        }

        val completed = subjects.count { it.second }
        val total = subjects.size
        val progress = if (total > 0) completed.toFloat() / total else 0f

        GlassCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Milestone Completion", style = MaterialTheme.typography.labelMedium)
                Text("$completed / $total Finished", style = MaterialTheme.typography.labelMedium, color = NovaEmerald)
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = NovaEmerald
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            subjects.forEachIndexed { idx, item ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth().clickable {
                        subjects = subjects.toMutableList().also {
                            it[idx] = item.first to !item.second
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = item.second,
                            onCheckedChange = { checked ->
                                subjects = subjects.toMutableList().also {
                                    it[idx] = item.first to checked
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = item.first,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (item.second) FontWeight.Normal else FontWeight.SemiBold,
                            color = if (item.second) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
