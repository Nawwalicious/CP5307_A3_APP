package com.example.eduapp.logic

data class Puzzle(
    val level: Int,
    val imagePath: String,
    val answer: Int
)

data class PuzzleLevel(
    val level: Int,
    val name: String,
    val puzzles: List<Puzzle>
)