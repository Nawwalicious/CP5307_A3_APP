package com.example.eduapp.screen

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.eduapp.database.DatabaseProvider
import com.example.eduapp.helper.AppPreferences
import com.example.eduapp.viewmodel.AppViewModel
import com.example.eduapp.viewmodel.AppViewModelFactory

/**
 * Settings.
 *
 * Every stored item can be deleted from here, and each destructive action asks
 * for confirmation before it runs.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    currentContext: Context,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val dao = remember { DatabaseProvider.getDatabase(currentContext).appDao() }
    val viewModel: AppViewModel = viewModel(factory = AppViewModelFactory(dao))
    val preferences = remember { AppPreferences(currentContext) }
    val players by viewModel.players.collectAsStateWithLifecycle(initialValue = emptyList())

    var soundOn by remember { mutableStateOf(preferences.soundEnabled) }
    var shuffleOn by remember { mutableStateOf(preferences.shuffleQuestions) }
    var showEraseDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SettingRow(
                title = "Sound effects",
                subtitle = "A short tone after each answer. Feedback is always shown on screen too.",
                checked = soundOn,
                onCheckedChange = {
                    soundOn = it
                    preferences.soundEnabled = it
                }
            )

            SettingRow(
                title = "Shuffle questions",
                subtitle = "Puzzles appear in a random order each round.",
                checked = shuffleOn,
                onCheckedChange = {
                    shuffleOn = it
                    preferences.shuffleQuestions = it
                }
            )

            Spacer(Modifier.height(4.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = "Your data",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Stored on this device: ${players.size} player name(s) and your recent scores. Nothing is uploaded, and there is no account or tracking.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            OutlinedButton(
                onClick = { navController.navigate("players") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
            ) { Text("Manage players") }

            OutlinedButton(
                onClick = { showEraseDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
            ) { Text("Erase everything") }

            Spacer(Modifier.height(4.dp))

            OutlinedButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
            ) { Text("Back") }
        }
    }

    if (showEraseDialog) {
        AlertDialog(
            onDismissRequest = { showEraseDialog = false },
            title = { Text("Erase everything?") },
            text = { Text("This deletes all ${players.size} player(s) and every saved score. It cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    players.forEach { viewModel.deletePlayer(it) }
                    viewModel.clearUsers()
                    showEraseDialog = false
                }) { Text("Erase") }
            },
            dismissButton = {
                TextButton(onClick = { showEraseDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SettingRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}