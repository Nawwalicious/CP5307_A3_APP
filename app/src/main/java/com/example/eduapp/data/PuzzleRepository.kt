package com.example.eduapp.data

import android.content.Context
import com.example.eduapp.logic.Puzzle
import com.example.eduapp.logic.PuzzleLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class PuzzleRepository(private val context: Context) {

    suspend fun loadLevels(): List<PuzzleLevel> = withContext(Dispatchers.IO) {
        val json = context.assets.open(ASSET_FILE).bufferedReader().use { it.readText() }
        parse(json)
    }

    /** Parses the puzzle document. Kept separate so it can be reused for remote content. */
    fun parse(json: String): List<PuzzleLevel> {
        val root = JSONObject(json)
        val levelsArray = root.getJSONArray("levels")

        return (0 until levelsArray.length()).map { i ->
            val levelObject = levelsArray.getJSONObject(i)
            val levelNumber = levelObject.getInt("level")
            val puzzlesArray = levelObject.getJSONArray("puzzles")

            val puzzles = (0 until puzzlesArray.length()).map { j ->
                val puzzleObject = puzzlesArray.getJSONObject(j)
                val image = puzzleObject.getString("image")
                Puzzle(
                    level = levelNumber,
                    imagePath = "$levelNumber/$image",
                    answer = puzzleObject.getInt("answer")
                )
            }

            PuzzleLevel(
                level = levelNumber,
                name = levelObject.optString("name", "Level $levelNumber"),
                puzzles = puzzles
            )
        }
    }

    companion object {
        const val ASSET_FILE = "puzzles.json"
    }
}