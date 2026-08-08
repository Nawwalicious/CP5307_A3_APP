//package com.example.eduapp
//
//import com.example.eduapp.logic.OptionGenerator
//import org.junit.Assert.assertEquals
//import org.junit.Assert.assertTrue
//import org.junit.Test
//
//class OptionGeneratorTest {
//
//    @Testss
//    fun alwaysReturnsFourOptions() {
//        for (answer in 0..60) {
//            val options = OptionGenerator.generate(answer, seed = answer.toLong())
//            assertEquals(OptionGenerator.OPTION_COUNT, options.size)
//        }
//    }
//
//    @Test
//    fun alwaysContainsTheCorrectAnswer() {
//        for (answer in 0..60) {
//            val options = OptionGenerator.generate(answer, seed = answer.toLong())
//            assertTrue("Missing correct answer $answer", options.contains(answer))
//        }
//    }
//
//    @Test
//    fun optionsAreUnique() {
//        for (answer in 0..60) {
//            val options = OptionGenerator.generate(answer, seed = answer.toLong())
//            assertEquals(options.size, options.toSet().size)
//        }
//    }
//
//    @Test
//    fun neverProducesNegativeOptions() {
//        for (answer in 0..60) {
//            val options = OptionGenerator.generate(answer, seed = 99L)
//            assertTrue(options.all { it >= 0 })
//        }
//    }
//
//    @Test
//    fun zeroAnswer_isHandled() {
//        val options = OptionGenerator.generate(0, seed = 1L)
//        assertEquals(4, options.size)
//        assertTrue(options.contains(0))
//    }
//
//    @Test
//    fun sameSeed_givesSameOptions() {
//        assertEquals(
//            OptionGenerator.generate(27, seed = 42L),
//            OptionGenerator.generate(27, seed = 42L)
//        )
//    }
//}