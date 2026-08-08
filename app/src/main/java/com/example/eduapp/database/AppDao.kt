package com.example.eduapp.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // ---------------------------------------------------------------- players

    @Insert
    suspend fun insertPlayer(player: Player)

    @Delete
    suspend fun deletePlayer(player: Player)

    /** Oldest slot first, so the three slots keep a stable on-screen order. */
    @Query("SELECT * FROM players ORDER BY createdAt ASC")
    fun getAllPlayers(): Flow<List<Player>>

    @Query("SELECT COUNT(*) FROM players")
    suspend fun countPlayers(): Int

    // ----------------------------------------------------------- score records

    @Insert
    suspend fun insert(user: User)

    /** Most recent result first. */
    @Query("SELECT * FROM users ORDER BY date DESC")
    fun getAllUsers(): Flow<List<User>>

    @Delete
    suspend fun deleteUser(user: User)

    @Query("DELETE FROM users")
    suspend fun deleteAll()

    @Query(
        "DELETE FROM users WHERE id NOT IN (" +
                "SELECT id FROM users ORDER BY date DESC LIMIT :keep" +
                ")"
    )
    suspend fun trimScores(keep: Int)
}