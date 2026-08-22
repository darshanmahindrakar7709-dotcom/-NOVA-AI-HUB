package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import com.example.viewmodel.AppViewModel

@Composable
fun SettingsScreen(viewModel: AppViewModel) {
    var showClearDataConfirm by remember { mutableStateOf(false) }
    var dataClearedNotice by remember { mutableStateOf(false) }

    if (showClearDataConfirm) {
        AlertDialog(
            onDismissRequest = { showClearDataConfirm = false },
            title = { Text("Clear All Local Data?") },
            text = { Text("This will remove all saved chat messages, study notes, flashcards, and to-do lists from this device.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllData()
                        showClearDataConfirm = false
                        dataClearedNotice = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Clear Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataConfirm = false }) { Text("Cancel") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Settings & Privacy",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        // Privacy Guarantee Card
        GlassCard(borderColor = NovaEmerald.copy(alpha = 0.5f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(NovaEmerald.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = NovaEmerald)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("NOVA Zero-Account Pledge", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = NovaEmerald)
                    Text("100% Free Forever", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "• No Login, Signup, or Passwords required.\n• No Subscriptions, Paywalls, or Credits.\n• All notes, cards, and chats remain on your device in a local Room database.\n• No tracking, telemetry, or third-party ads.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Data & Storage Management
        GlassCard {
            Text("Data & Storage", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { showClearDataConfirm = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.DeleteSweep, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Clear All Local App Data")
            }

            if (dataClearedNotice) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("All local data has been successfully cleared.", color = NovaEmerald, style = MaterialTheme.typography.bodySmall)
            }
        }

        // App Information Card
        GlassCard {
            Text("About NOVA AI HUB", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            SettingsInfoRow("App Name", "NOVA AI HUB")
            SettingsInfoRow("Version", "1.0.0 (Build 2026)")
            SettingsInfoRow("Architecture", "Jetpack Compose + Material 3 + Room DB")
            SettingsInfoRow("Environment", "Google AI Studio Platform")
            SettingsInfoRow("Status", "Production Ready • 100% Free")
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun SettingsInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = NovaCyan)
    }
}
