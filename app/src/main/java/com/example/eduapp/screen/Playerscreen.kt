package com.example.eduapp.screen

import android.content.Context
import android.net.Uri
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import com.example.eduapp.database.Player
import com.example.eduapp.logic.GameRules
import com.example.eduapp.viewmodel.AppViewModel
import com.example.eduapp.viewmodel.AppViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    currentContext: Context,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val dao = remember { DatabaseProvider.getDatabase(currentContext).appDao() }
    val viewModel: AppViewModel = viewModel(factory = AppViewModelFactory(dao))
    val players by viewModel.players.collectAsStateWithLifecycle(initialValue = emptyList())

    var newName by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }
    var playerToDelete by remember { mutableStateOf<Player?>(null) }

    val slotsFull = GameRules.isPlayerLimitReached(players.size)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Choose a player") },
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "${players.size} of ${GameRules.MAX_PLAYERS} slots used",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (players.isEmpty()) {
                Text(
                    text = "No players yet. Create one below to start.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            players.forEach { player ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(start = 16.dp, end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = player.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = {
                                navController.navigate("levels/${Uri.encode(player.name)}")
                            },
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) { Text("Play") }
                        IconButton(
                            onClick = { playerToDelete = player },
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete player ${player.name}"
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Add a player",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = newName,
                onValueChange = {
                    newName = it
                    errorText = null
                },
                label = { Text("Player name") },
                singleLine = true,
                enabled = !slotsFull,
                isError = errorText != null,
                supportingText = {
                    Text(
                        text = errorText
                            ?: if (slotsFull) {
                                "All slots are full. Delete a player to free one."
                            } else {
                                "2 to 12 characters, letters and numbers."
                            }
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    viewModel.addPlayer(newName) { error ->
                        if (error == null) newName = "" else errorText = error
                    }
                },
                enabled = !slotsFull && newName.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
            ) { Text("Create player") }

            Spacer(Modifier.height(4.dp))

            TextButton(onClick = { navController.popBackStack() }) {
                Text("Back")
            }
        }
    }

    // Deletion always asks first, and names what will be lost.
    playerToDelete?.let { player ->
        AlertDialog(
            onDismissRequest = { playerToDelete = null },
            title = { Text("Delete ${player.name}?") },
            text = {
                Text("This frees up a player slot. Scores already saved under this name will stay in the high score list.")
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deletePlayer(player)
                    playerToDelete = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { playerToDelete = null }) { Text("Cancel") }
            }
        )
    }
}