package com.example.eduapp.logic
object GameRules {
    const val MAX_PLAYERS = 3
    const val MAX_SCORES = 10
    const val LEVEL_COUNT = 3

    private const val MIN_NAME_LENGTH = 2
    private const val MAX_NAME_LENGTH = 12
    private val ALLOWED_NAME = Regex("^[A-Za-z0-9 ]+$")

    /**
     * Validates a proposed player name.
     *
     * @return null when the name is acceptable, otherwise a message that can be
     *         shown directly to the user.
     */
    fun validatePlayerName(rawName: String, existingNames: List<String>): String? {
        val name = rawName.trim()

        return when {
            name.isEmpty() ->
                "Please enter a name."

            name.length < MIN_NAME_LENGTH ->
                "Name must be at least $MIN_NAME_LENGTH characters."

            name.length > MAX_NAME_LENGTH ->
                "Name must be $MAX_NAME_LENGTH characters or fewer."

            !ALLOWED_NAME.matches(name) ->
                "Use letters, numbers and spaces only."

            existingNames.any { it.trim().equals(name, ignoreCase = true) } ->
                "That name is already taken."

            else -> null
        }
    }

    /** True when a new player cannot be added because every slot is in use. */
    fun isPlayerLimitReached(currentPlayerCount: Int): Boolean =
        currentPlayerCount >= MAX_PLAYERS

    /** Number of free slots remaining. */
    fun freeSlots(currentPlayerCount: Int): Int =
        (MAX_PLAYERS - currentPlayerCount).coerceAtLeast(0)
}