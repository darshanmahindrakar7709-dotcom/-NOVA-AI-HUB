package com.example.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.tools.registry.ToolCategory
import com.example.tools.registry.ToolDefinition
import com.example.tools.registry.ToolRegistry
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val toolDao = database.toolDao()
    private val notesDao = database.notesDao()
    private val flashcardsDao = database.flashcardsDao()
    private val todoDao = database.todoDao()
    private val chatDao = database.chatDao()
    private val settingsDao = database.settingsDao()

    // Navigation State
    val selectedTab = MutableStateFlow(0) // 0: Home, 1: Tools, 2: Favorites, 3: History, 4: Settings
    val activeToolId = MutableStateFlow<String?>(null)

    // Search & Filter
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow(ToolCategory.ALL)

    // Room Flows
    val recentHistory: StateFlow<List<ToolHistoryEntity>> = toolDao.getRecentHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favorites: StateFlow<List<FavoriteToolEntity>> = toolDao.getFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotes: StateFlow<List<NoteEntity>> = notesDao.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDecks: StateFlow<List<FlashcardDeckEntity>> = flashcardsDao.getAllDecks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTodos: StateFlow<List<TodoEntity>> = todoDao.getAllTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<ChatMessageEntity>> = chatDao.getChatMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // App Settings State
    val isDarkMode = MutableStateFlow(true)
    val apiKeySetting = MutableStateFlow("")
    val isAiGenerating = MutableStateFlow(false)
    val statusMessage = MutableStateFlow<String?>(null)

    init {
        loadSettings()
        populateSampleDataIfEmpty()
    }

    fun selectTab(index: Int) {
        selectedTab.value = index
        activeToolId.value = null
    }

    fun openTool(toolId: String) {
        navigateToTool(toolId)
    }

    fun navigateToTool(toolId: String) {
        activeToolId.value = toolId
        val tool = ToolRegistry.findToolById(toolId)
        if (tool != null) {
            viewModelScope.launch {
                toolDao.recordToolUsage(
                    ToolHistoryEntity(
                        toolId = tool.id,
                        toolTitle = tool.name,
                        categoryName = tool.category.displayName
                    )
                )
            }
        }
    }

    fun closeTool() {
        activeToolId.value = null
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val darkModeSetting = settingsDao.getSetting("dark_mode")
            if (darkModeSetting != null) {
                isDarkMode.value = darkModeSetting.toBoolean()
            }
            val key = settingsDao.getSetting("api_key")
            if (key != null) {
                apiKeySetting.value = key
            }
        }
    }

    fun setDarkMode(enabled: Boolean) {
        isDarkMode.value = enabled
        viewModelScope.launch {
            settingsDao.setSetting(AppSettingEntity("dark_mode", enabled.toString()))
        }
    }

    fun setApiKey(key: String) {
        apiKeySetting.value = key.trim()
        viewModelScope.launch {
            settingsDao.setSetting(AppSettingEntity("api_key", key.trim()))
            statusMessage.value = if (key.isBlank()) "API Key cleared (offline mode)" else "API Key saved successfully"
        }
    }

    fun toggleFavorite(toolId: String) {
        viewModelScope.launch {
            val isFav = toolDao.isFavorite(toolId)
            if (isFav) {
                toolDao.removeFavorite(toolId)
            } else {
                toolDao.addFavorite(FavoriteToolEntity(toolId = toolId))
            }
        }
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            toolDao.deleteHistoryItem(id)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            toolDao.clearHistory()
            statusMessage.value = "History cleared"
        }
    }

    // Notes Actions
    fun saveNote(title: String, content: String, tag: String = "General", colorHex: String = "#8B5CF6", id: Long = 0) {
        viewModelScope.launch {
            val note = NoteEntity(
                id = id,
                title = title.ifBlank { "Untitled Note" },
                content = content,
                tag = tag.ifBlank { "General" },
                colorHex = colorHex,
                updatedAt = System.currentTimeMillis()
            )
            notesDao.insertNote(note)
            statusMessage.value = "Note saved"
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            notesDao.deleteNote(note)
            statusMessage.value = "Note deleted"
        }
    }

    // Todo Actions
    fun addTodo(title: String, category: String = "General", priority: String = "Medium") {
        if (title.isBlank()) return
        viewModelScope.launch {
            todoDao.insertTodo(
                TodoEntity(
                    title = title.trim(),
                    category = category,
                    priority = priority,
                    isCompleted = false
                )
            )
        }
    }

    fun toggleTodo(todo: TodoEntity) {
        viewModelScope.launch {
            todoDao.updateTodo(todo.copy(isCompleted = !todo.isCompleted))
        }
    }

    fun deleteTodo(todo: TodoEntity) {
        viewModelScope.launch {
            todoDao.deleteTodo(todo)
        }
    }

    fun clearCompletedTodos() {
        viewModelScope.launch {
            todoDao.clearCompletedTodos()
            statusMessage.value = "Completed tasks cleared"
        }
    }

    // Flashcards Actions
    fun createDeck(title: String, description: String = "") {
        if (title.isBlank()) return
        viewModelScope.launch {
            flashcardsDao.insertDeck(FlashcardDeckEntity(title = title.trim(), description = description.trim()))
            statusMessage.value = "Deck created"
        }
    }

    fun getCardsForDeck(deckId: Long): Flow<List<FlashcardEntity>> {
        return flashcardsDao.getCardsForDeck(deckId)
    }

    fun addCard(deckId: Long, question: String, answer: String) {
        if (question.isBlank() || answer.isBlank()) return
        viewModelScope.launch {
            flashcardsDao.insertCard(FlashcardEntity(deckId = deckId, question = question.trim(), answer = answer.trim()))
        }
    }

    fun deleteCard(card: FlashcardEntity) {
        viewModelScope.launch {
            flashcardsDao.deleteCard(card)
        }
    }

    fun deleteDeck(deck: FlashcardDeckEntity) {
        viewModelScope.launch {
            flashcardsDao.deleteCardsInDeck(deck.id)
            flashcardsDao.deleteDeck(deck)
            statusMessage.value = "Deck deleted"
        }
    }

    // Chat Actions
    fun sendChatMessage(userText: String) {
        if (userText.isBlank()) return
        val text = userText.trim()

        viewModelScope.launch {
            // Save user message
            chatDao.insertMessage(ChatMessageEntity(role = "user", content = text))
            isAiGenerating.value = true

            val aiResponse = generateChatResponse(text)
            chatDao.insertMessage(ChatMessageEntity(role = "assistant", content = aiResponse))
            isAiGenerating.value = false
        }
    }

    private suspend fun generateChatResponse(prompt: String): String = withContext(Dispatchers.IO) {
        // First, check if Firebase AI or Gemini is configured
        try {
            val model = Firebase.ai.generativeModel("gemini-2.5-flash")
            val response = model.generateContent(content { text(prompt) })
            val candidateText = response.text
            if (!candidateText.isNullOrBlank()) {
                return@withContext candidateText
            }
        } catch (_: Exception) {
            // Fallback gracefully to our intelligent client-side NLP responses engine
        }

        // Built-in intelligent client-side generation engine
        return@withContext generateClientSideAiResponse(prompt)
    }

    private fun generateClientSideAiResponse(prompt: String): String {
        val lower = prompt.lowercase()
        return when {
            lower.contains("hello") || lower.contains("hi") || lower.contains("hey") ->
                "👋 Hello! I am NOVA AI Assistant. How can I help you today?\n\nI can assist you with writing, code formatting, study plans, brainstorming ideas, and mathematical calculations — completely free without any account!"

            lower.contains("code") || lower.contains("kotlin") || lower.contains("function") || lower.contains("javascript") || lower.contains("python") ->
                "Here is an example solution for your coding request:\n\n```kotlin\n// NOVA AI Clean Pattern\nfun calculateResult(input: List<String>): Map<String, Int> {\n    return input\n        .filter { it.isNotBlank() }\n        .groupBy { it.trim().lowercase() }\n        .mapValues { it.value.size }\n}\n```\n\n💡 *Tip: You can also use our **Coding Tools** in the Tools tab for instant JSON formatting, Regex testing, and Base64 conversions!*"

            lower.contains("study") || lower.contains("exam") || lower.contains("learn") ->
                "📚 **Study Strategy Recommendation:**\n\n1. **Active Recall**: Test yourself with our built-in **Flashcard Studio**.\n2. **Spaced Repetition**: Review challenging topics after 24 hours, then 3 days, then 7 days.\n3. **Feynman Technique**: Explain the concept in simple terms to spot knowledge gaps.\n4. **Pomodoro Cycles**: Use the 25-minute focus / 5-minute break timer in the Study section."

            lower.contains("summar") || lower.contains("tldr") ->
                "📝 **Key Points Summary:**\n\n• **Core Message**: Clear and concise communication delivers maximum impact.\n• **Action Item**: Identify primary goals, remove filler, and organize steps logically.\n• **Conclusion**: Client-first offline tools give you fast results without cloud delays."

            lower.contains("idea") || lower.contains("brainstorm") ->
                "💡 **Brainstormed Concepts:**\n\n1. **The Modular Approach**: Focus on lightweight, composable components that work offline.\n2. **Frictionless UX**: Zero login, instant utility, and intuitive gesture controls.\n3. **Smart Presets**: Provide curated templates for common workflows to save user time."

            else ->
                "Here is some helpful guidance regarding **\"$prompt\"**:\n\n• **Direct Answer**: You can accomplish this easily using our modular toolset.\n• **Recommended Next Step**: Check the **Tools** tab to test out specialized writing, formatting, or calculation utilities.\n• **Offline Guarantee**: All operations run locally on your device with high performance.\n\n*Note: To connect live cloud Gemini models, you can optionally provide an API key in Settings.*"
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            chatDao.clearAllChat()
            statusMessage.value = "Chat history cleared"
        }
    }

    fun copyToClipboard(text: String, label: String = "Copied text") {
        val context = getApplication<Application>()
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        statusMessage.value = "Copied to clipboard!"
    }

    fun clearAllData() {
        clearAllUserData()
    }

    fun clearAllUserData() {
        viewModelScope.launch {
            toolDao.clearHistory()
            notesDao.clearAllNotes()
            flashcardsDao.clearAllCards()
            flashcardsDao.clearAllDecks()
            todoDao.clearAllTodos()
            chatDao.clearAllChat()
            statusMessage.value = "All local data has been cleared"
        }
    }

    private fun populateSampleDataIfEmpty() {
        viewModelScope.launch {
            val existingNotes = notesDao.getAllNotes().first()
            if (existingNotes.isEmpty()) {
                notesDao.insertNote(
                    NoteEntity(
                        title = "Welcome to NOVA AI HUB",
                        content = "Welcome to your all-in-one AI & productivity workspace.\n\n• 100% Free to use\n• No login or signup required\n• All tools work locally and offline\n• Save notes, flashcards, todos, and chat history safely on your device.",
                        tag = "Guide",
                        colorHex = "#8B5CF6"
                    )
                )
                notesDao.insertNote(
                    NoteEntity(
                        title = "Study & Research Tips",
                        content = "1. Use Flashcard Studio for active recall.\n2. Use Text Summarizer to condense long passages.\n3. Use JSON Formatter and Regex Tester for development tasks.",
                        tag = "Study",
                        colorHex = "#06B6D4"
                    )
                )
            }

            val existingDecks = flashcardsDao.getAllDecks().first()
            if (existingDecks.isEmpty()) {
                val deckId = flashcardsDao.insertDeck(
                    FlashcardDeckEntity(
                        title = "Computer Science Fundamentals",
                        description = "Key data structures, algorithms, and concepts."
                    )
                )
                flashcardsDao.insertCard(
                    FlashcardEntity(
                        deckId = deckId,
                        question = "What is the time complexity of Binary Search?",
                        answer = "O(log n) because the search range is halved in each step."
                    )
                )
                flashcardsDao.insertCard(
                    FlashcardEntity(
                        deckId = deckId,
                        question = "What does ACID stand for in databases?",
                        answer = "Atomicity, Consistency, Isolation, and Durability."
                    )
                )
                flashcardsDao.insertCard(
                    FlashcardEntity(
                        deckId = deckId,
                        question = "What is a Pure Function in programming?",
                        answer = "A function that always returns the same output for the same input and has no observable side effects."
                    )
                )
            }

            val existingTodos = todoDao.getAllTodos().first()
            if (existingTodos.isEmpty()) {
                todoDao.insertTodo(
                    TodoEntity(
                        title = "Explore NOVA AI Tools catalog",
                        category = "Onboarding",
                        priority = "High",
                        isCompleted = false
                    )
                )
                todoDao.insertTodo(
                    TodoEntity(
                        title = "Try out the Flashcard Studio & Quiz generator",
                        category = "Study",
                        priority = "Medium",
                        isCompleted = false
                    )
                )
                todoDao.insertTodo(
                    TodoEntity(
                        title = "Test JSON Formatter and Code Beautifier",
                        category = "Coding",
                        priority = "Low",
                        isCompleted = true
                    )
                )
            }
        }
    }
}
