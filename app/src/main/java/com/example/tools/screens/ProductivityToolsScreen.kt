package com.example.tools.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TodoEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.AppViewModel
import kotlinx.coroutines.delay
import java.security.SecureRandom
import kotlin.math.*

@Composable
fun ProductivityToolsScreen(
    toolId: String,
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val favorites by viewModel.favorites.collectAsState()
    val isFav = favorites.any { it.toolId == toolId }

    var selectedTab by remember(toolId) {
        mutableIntStateOf(
            when (toolId) {
                "todo_list" -> 0
                "pomodoro_stopwatch" -> 1
                "calculator" -> 2
                "unit_converter" -> 3
                "random_generator" -> 4
                else -> 0
            }
        )
    }

    val tabTitles = listOf("To-Do Tasks", "Stopwatch", "Calculator", "Unit Converter", "Random Suite")

    Scaffold(
        topBar = {
            ToolTopBar(
                title = "Productivity Suite",
                categoryName = "Productivity",
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
                    0 -> TodoManagerView(viewModel)
                    1 -> StopwatchPrecisionView(viewModel)
                    2 -> CalculatorView(viewModel)
                    3 -> UnitConverterView(viewModel)
                    4 -> RandomSuiteView(viewModel)
                }
            }
        }
    }
}

// 1. TO-DO MANAGER
@Composable
fun TodoManagerView(viewModel: AppViewModel) {
    val todos by viewModel.allTodos.collectAsState()
    var newTodoTitle by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Work") }
    var selectedPriority by remember { mutableStateOf("Medium") }
    var selectedFilter by remember { mutableStateOf("All") }

    val categories = listOf("All", "Work", "Study", "Personal", "Code")
    val priorities = listOf("High", "Medium", "Low")

    val filteredTodos = todos.filter {
        selectedFilter == "All" || it.category.equals(selectedFilter, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        GlassCard {
            Text("Add New Task", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newTodoTitle,
                    onValueChange = { newTodoTitle = it },
                    placeholder = { Text("What needs to be done?") },
                    modifier = Modifier.weight(1f).testTag("todo_input_field"),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        viewModel.addTodo(newTodoTitle, selectedCategory, selectedPriority)
                        newTodoTitle = ""
                    },
                    enabled = newTodoTitle.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NovaViolet),
                    modifier = Modifier.height(52.dp).testTag("add_todo_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("Work", "Study", "Personal").forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, style = MaterialTheme.typography.bodySmall) }
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    priorities.forEach { p ->
                        FilterChip(
                            selected = selectedPriority == p,
                            onClick = { selectedPriority = p },
                            label = { Text(p, style = MaterialTheme.typography.bodySmall) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedFilter == cat,
                        onClick = { selectedFilter = cat },
                        label = { Text(cat, style = MaterialTheme.typography.bodySmall) }
                    )
                }
            }

            if (todos.any { it.isCompleted }) {
                TextButton(onClick = { viewModel.clearCompletedTodos() }) {
                    Text("Clear Done", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredTodos.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.CheckCircleOutline,
                title = "All clear!",
                subtitle = "No tasks found in this view. Enjoy your productive day!"
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredTodos) { todo ->
                    val priorityColor = when (todo.priority) {
                        "High" -> MaterialTheme.colorScheme.error
                        "Medium" -> NovaAmber
                        else -> NovaCyan
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth().testTag("todo_item_${todo.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = todo.isCompleted,
                                onCheckedChange = { viewModel.toggleTodo(todo) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = todo.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (todo.isCompleted) FontWeight.Normal else FontWeight.SemiBold,
                                    color = if (todo.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                    textDecoration = if (todo.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(todo.category, style = MaterialTheme.typography.labelSmall, color = NovaCyan)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(modifier = Modifier.size(6.dp).background(priorityColor, CircleShape))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(todo.priority, style = MaterialTheme.typography.labelSmall, color = priorityColor)
                                }
                            }
                            IconButton(onClick = { viewModel.deleteTodo(todo) }) {
                                Icon(Icons.Default.Close, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

// 2. STOPWATCH & PRECISION TIMER
@Composable
fun StopwatchPrecisionView(viewModel: AppViewModel) {
    var elapsedMillis by remember { mutableLongStateOf(0L) }
    var isRunning by remember { mutableStateOf(false) }
    val laps = remember { mutableStateListOf<Long>() }

    LaunchedEffect(isRunning) {
        val startTime = System.currentTimeMillis() - elapsedMillis
        while (isRunning) {
            elapsedMillis = System.currentTimeMillis() - startTime
            delay(30L)
        }
    }

    val minutes = (elapsedMillis / 60000)
    val seconds = (elapsedMillis % 60000) / 1000
    val millis = (elapsedMillis % 1000) / 10

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = String.format("%02d:%02d.%02d", minutes, seconds, millis),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = NovaCyan
                )
                Text("HIGH PRECISION STOPWATCH", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(20.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        onClick = { isRunning = !isRunning },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (isRunning) NovaAmber else NovaViolet),
                        modifier = Modifier.width(120.dp).height(48.dp)
                    ) {
                        Text(if (isRunning) "Pause" else "Start")
                    }

                    if (isRunning) {
                        FilledTonalButton(
                            onClick = { laps.add(0, elapsedMillis) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.width(100.dp).height(48.dp)
                        ) {
                            Text("Lap")
                        }
                    } else if (elapsedMillis > 0) {
                        OutlinedButton(
                            onClick = {
                                isRunning = false
                                elapsedMillis = 0L
                                laps.clear()
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.width(100.dp).height(48.dp)
                        ) {
                            Text("Reset")
                        }
                    }
                }
            }
        }

        if (laps.isNotEmpty()) {
            GlassCard(modifier = Modifier.weight(1f).fillMaxWidth()) {
                Text("Recorded Laps", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(laps.size) { idx ->
                        val lapTime = laps[idx]
                        val lMin = lapTime / 60000
                        val lSec = (lapTime % 60000) / 1000
                        val lMil = (lapTime % 1000) / 10

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Lap #${laps.size - idx}", style = MaterialTheme.typography.bodyMedium, color = NovaCyan)
                            Text(String.format("%02d:%02d.%02d", lMin, lSec, lMil), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// 3. CALCULATOR
@Composable
fun CalculatorView(viewModel: AppViewModel) {
    var expression by remember { mutableStateOf("0") }
    var result by remember { mutableStateOf("0") }

    fun append(char: String) {
        if (expression == "0" && char !in "+-×÷%.") {
            expression = char
        } else {
            expression += char
        }
    }

    fun calculate() {
        try {
            val exp = expression
                .replace("×", "*")
                .replace("÷", "/")
            val evalResult = evaluateSimpleExpression(exp)
            result = if (evalResult % 1.0 == 0.0) {
                evalResult.toLong().toString()
            } else {
                String.format("%.4f", evalResult)
            }
        } catch (_: Exception) {
            result = "Error"
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Display Screen
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = SpaceSurfaceElevated,
            modifier = Modifier.fillMaxWidth().height(120.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = expression,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1
                )
                Text(
                    text = result,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = NovaCyan,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1
                )
            }
        }

        // Keypad Grid
        val buttonRows = listOf(
            listOf("C", "(", ")", "÷"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "+"),
            listOf("DEL", "0", ".", "=")
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            buttonRows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { key ->
                        val isOp = key in listOf("÷", "×", "-", "+", "=")
                        val isAction = key in listOf("C", "DEL")
                        val containerColor = when {
                            key == "=" -> NovaViolet
                            isOp -> NovaVioletDark
                            isAction -> MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                            else -> SpaceSurfaceElevated
                        }

                        Button(
                            onClick = {
                                when (key) {
                                    "C" -> {
                                        expression = "0"
                                        result = "0"
                                    }
                                    "DEL" -> {
                                        if (expression.length > 1) {
                                            expression = expression.dropLast(1)
                                        } else {
                                            expression = "0"
                                        }
                                    }
                                    "=" -> calculate()
                                    else -> append(key)
                                }
                            },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = containerColor),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = key,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

fun evaluateSimpleExpression(expr: String): Double {
    // Basic safe parser for +, -, *, /
    val tokens = mutableListOf<String>()
    var currentNumber = ""
    for (char in expr) {
        if (char in "+-*/()") {
            if (currentNumber.isNotEmpty()) {
                tokens.add(currentNumber)
                currentNumber = ""
            }
            tokens.add(char.toString())
        } else {
            currentNumber += char
        }
    }
    if (currentNumber.isNotEmpty()) tokens.add(currentNumber)

    if (tokens.isEmpty()) return 0.0

    // Compute * and / first
    val processed = mutableListOf<String>()
    var i = 0
    while (i < tokens.size) {
        if (tokens[i] == "*" || tokens[i] == "/") {
            val op = tokens[i]
            val prev = processed.removeAt(processed.size - 1).toDouble()
            val next = tokens[i + 1].toDouble()
            val res = if (op == "*") prev * next else prev / next
            processed.add(res.toString())
            i += 2
        } else {
            processed.add(tokens[i])
            i++
        }
    }

    // Compute + and -
    var finalResult = processed[0].toDoubleOrNull() ?: 0.0
    var j = 1
    while (j < processed.size) {
        val op = processed[j]
        val next = processed[j + 1].toDoubleOrNull() ?: 0.0
        if (op == "+") finalResult += next
        if (op == "-") finalResult -= next
        j += 2
    }

    return finalResult
}

// 4. MULTI-UNIT CONVERTER
@Composable
fun UnitConverterView(viewModel: AppViewModel) {
    var category by remember { mutableStateOf("Length") }
    var inputValue by remember { mutableStateOf("10") }
    var fromUnit by remember { mutableStateOf("Meters (m)") }
    var toUnit by remember { mutableStateOf("Feet (ft)") }

    val categories = listOf("Length", "Weight", "Temperature", "Digital Storage")

    val units = when (category) {
        "Length" -> listOf("Meters (m)", "Kilometers (km)", "Centimeters (cm)", "Miles (mi)", "Feet (ft)", "Inches (in)")
        "Weight" -> listOf("Kilograms (kg)", "Grams (g)", "Pounds (lbs)", "Ounces (oz)", "Metric Tons")
        "Temperature" -> listOf("Celsius (°C)", "Fahrenheit (°F)", "Kelvin (K)")
        else -> listOf("Megabytes (MB)", "Gigabytes (GB)", "Kilobytes (KB)", "Terabytes (TB)", "Bytes")
    }

    val convertedValue = remember(inputValue, fromUnit, toUnit, category) {
        val input = inputValue.toDoubleOrNull() ?: 0.0
        when (category) {
            "Length" -> {
                val inMeters = when (fromUnit) {
                    "Kilometers (km)" -> input * 1000
                    "Centimeters (cm)" -> input * 0.01
                    "Miles (mi)" -> input * 1609.34
                    "Feet (ft)" -> input * 0.3048
                    "Inches (in)" -> input * 0.0254
                    else -> input
                }
                when (toUnit) {
                    "Kilometers (km)" -> inMeters / 1000
                    "Centimeters (cm)" -> inMeters * 100
                    "Miles (mi)" -> inMeters / 1609.34
                    "Feet (ft)" -> inMeters / 0.3048
                    "Inches (in)" -> inMeters / 0.0254
                    else -> inMeters
                }
            }
            "Temperature" -> {
                val inC = when (fromUnit) {
                    "Fahrenheit (°F)" -> (input - 32) * 5 / 9
                    "Kelvin (K)" -> input - 273.15
                    else -> input
                }
                when (toUnit) {
                    "Fahrenheit (°F)" -> (inC * 9 / 5) + 32
                    "Kelvin (K)" -> inC + 273.15
                    else -> inC
                }
            }
            else -> input * 1.0
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        GlassCard {
            Text("Universal Unit Converter", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(categories) { cat ->
                    FilterChip(
                        selected = category == cat,
                        onClick = {
                            category = cat
                            fromUnit = units.first()
                            toUnit = units.getOrElse(1) { units.first() }
                        },
                        label = { Text(cat) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = inputValue,
                onValueChange = { inputValue = it },
                label = { Text("Input Value") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = fromUnit,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("From") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = toUnit,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("To") },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        GlassCard {
            Text("Conversion Result", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = NovaCyan)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$inputValue $fromUnit = ${String.format("%.4f", convertedValue)} $toUnit",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

// 5. RANDOM SUITE
@Composable
fun RandomSuiteView(viewModel: AppViewModel) {
    var minNum by remember { mutableStateOf("1") }
    var maxNum by remember { mutableStateOf("100") }
    var randomNumber by remember { mutableStateOf("42") }

    var diceValue by remember { mutableIntStateOf(6) }
    var coinValue by remember { mutableStateOf("HEADS") }
    var passwordLength by remember { mutableFloatStateOf(16f) }
    var generatedPassword by remember { mutableStateOf("") }

    val secureRandom = remember { SecureRandom() }

    fun generatePassword() {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+-=[]{}"
        val pass = (1..passwordLength.toInt())
            .map { chars[secureRandom.nextInt(chars.length)] }
            .joinToString("")
        generatedPassword = pass
    }

    LaunchedEffect(Unit) {
        generatePassword()
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Number Range Generator
        GlassCard {
            Text("Random Number Generator", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = minNum,
                    onValueChange = { minNum = it },
                    label = { Text("Min") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )
                OutlinedTextField(
                    value = maxNum,
                    onValueChange = { maxNum = it },
                    label = { Text("Max") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    val min = minNum.toIntOrNull() ?: 1
                    val max = maxNum.toIntOrNull() ?: 100
                    if (max >= min) {
                        randomNumber = (min..max).random().toString()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NovaViolet)
            ) {
                Text("Generate Number: $randomNumber", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Dice & Coin Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GlassCard(modifier = Modifier.weight(1f)) {
                Text("Dice Roller (D6)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { diceValue = (1..6).random() },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NovaCyanDark)
                ) {
                    Text("🎲 $diceValue", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            }

            GlassCard(modifier = Modifier.weight(1f)) {
                Text("Coin Flipper", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { coinValue = if (listOf(true, false).random()) "HEADS" else "TAILS" },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NovaPink)
                ) {
                    Text("🪙 $coinValue", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Password Generator
        GlassCard {
            Text("Strong Password Generator", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Text("Length: ${passwordLength.toInt()} characters", style = MaterialTheme.typography.labelSmall, color = NovaCyan)
            Slider(
                value = passwordLength,
                onValueChange = {
                    passwordLength = it
                    generatePassword()
                },
                valueRange = 8f..32f
            )

            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = SpaceSurfaceElevated,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = generatedPassword,
                        style = MaterialTheme.typography.bodyLarge,
                        fontFamily = FontFamily.Monospace,
                        color = NovaEmerald,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { viewModel.copyToClipboard(generatedPassword) }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy Password")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { generatePassword() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NovaViolet)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Regenerate Secure Password")
            }
        }
    }
}
