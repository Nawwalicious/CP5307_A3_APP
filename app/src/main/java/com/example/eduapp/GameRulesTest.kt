package com.example.eduapp

import com.example.eduapp.logic.GameRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test


class GameRulesTest {

    @Test
    fun validName_isAccepted() {
        assertNull(GameRules.validatePlayerName("Nawwal", emptyList()))
    }

    @Test
    fun nameIsTrimmedBeforeValidation() {
        assertNull(GameRules.validatePlayerName("  Sam  ", emptyList()))
    }

    @Test
    fun blankName_isRejected() {
        assertNotNull(GameRules.validatePlayerName("   ", emptyList()))
    }

    @Test
    fun tooShortName_isRejected() {
        assertNotNull(GameRules.validatePlayerName("A", emptyList()))
    }

    @Test
    fun tooLongName_isRejected() {
        assertNotNull(GameRules.validatePlayerName("ThisNameIsFarTooLong", emptyList()))
    }

    @Test
    fun nameWithSymbols_isRejected() {
        assertNotNull(GameRules.validatePlayerName("Bob<script>", emptyList()))
    }

    @Test
    fun duplicateName_isRejectedRegardlessOfCase() {
        val error = GameRules.validatePlayerName("nawwal", listOf("Nawwal"))
        assertNotNull(error)
    }

    @Test
    fun playerLimit_isReachedAtThree() {
        assertFalse(GameRules.isPlayerLimitReached(2))
        assertTrue(GameRules.isPlayerLimitReached(3))
        assertTrue(GameRules.isPlayerLimitReached(4))
    }

    @Test
    fun freeSlots_neverGoesNegative() {
        assertEquals(3, GameRules.freeSlots(0))
        assertEquals(1, GameRules.freeSlots(2))
        assertEquals(0, GameRules.freeSlots(5))
    }
}