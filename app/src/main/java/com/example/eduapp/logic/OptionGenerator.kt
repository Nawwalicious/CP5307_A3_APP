package com.example.eduapp.logic

import kotlin.random.Random
object OptionGenerator {

    const val OPTION_COUNT = 4

    fun generate(correctAnswer: Int, seed: Long): List<Int> {
        val random = Random(seed)
        val candidates = LinkedHashSet<Int>()

        // Near misses first: these are the answers a learner would plausibly
        // arrive at from a small arithmetic slip.
        val offsets = listOf(1, -1, 2, -2, 3, -3, 5, -5, 10, -10)
        for (offset in offsets.shuffled(random)) {
            val value = correctAnswer + offset
            if (value >= 0 && value != correctAnswer) candidates.add(value)
        }

        // Then errors of scale: doubling or halving part of the working.
        listOf(correctAnswer * 2, correctAnswer / 2, correctAnswer + correctAnswer / 2)
            .forEach { if (it >= 0 && it != correctAnswer) candidates.add(it) }

        // Guarantee enough distractors even for awkward values such as 0.
        var padding = 11
        while (candidates.size < OPTION_COUNT - 1) {
            val value = correctAnswer + padding
            if (value != correctAnswer) candidates.add(value)
            padding++
        }

        val distractors = candidates.toList().shuffled(random).take(OPTION_COUNT - 1)
        return (distractors + correctAnswer).shuffled(random)
    }
}