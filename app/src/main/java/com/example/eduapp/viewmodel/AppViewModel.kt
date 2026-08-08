package com.example.eduapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eduapp.database.AppDao
import com.example.eduapp.database.Player
import com.example.eduapp.database.User
import com.example.eduapp.logic.GameRules
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AppViewModel(private val dao: AppDao) : ViewModel() {

    /** The three player slots, oldest first. */
    val players: Flow<List<Player>> = dao.getAllPlayers()

    /** Saved results, most recent first, capped at GameRules.MAX_SCORES. */
    val users: Flow<List<User>> = dao.getAllUsers()


    fun addPlayer(rawName: String, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            val existing = dao.getAllPlayers().first()

            if (GameRules.isPlayerLimitReached(existing.size)) {
                onResult("All ${GameRules.MAX_PLAYERS} slots are full. Delete a player first.")
                return@launch
            }

            val error = GameRules.validatePlayerName(rawName, existing.map { it.name })
            if (error != null) {
                onResult(error)
                return@launch
            }

            dao.insertPlayer(Player(name = rawName.trim()))
            onResult(null)
        }
    }

    fun deletePlayer(player: Player) {
        viewModelScope.launch { dao.deletePlayer(player) }
    }

    fun saveScore(username: String, level: Int, score: Int) {
        viewModelScope.launch {
            dao.insert(
                User(
                    username = username,
                    level = level.toString(),
                    score = score
                )
            )
            dao.trimScores(GameRules.MAX_SCORES)
        }
    }

    fun deleteScore(user: User) {
        viewModelScope.launch { dao.deleteUser(user) }
    }

    fun clearUsers() {
        viewModelScope.launch { dao.deleteAll() }
    }
}