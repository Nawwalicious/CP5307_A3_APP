package com.example.eduapp.helper

import android.media.AudioManager
import android.media.ToneGenerator
object SoundPlayer {

    private const val DURATION_MS = 180

    fun playCorrect(enabled: Boolean) = play(enabled, ToneGenerator.TONE_PROP_BEEP)

    fun playWrong(enabled: Boolean) = play(enabled, ToneGenerator.TONE_PROP_NACK)

    private fun play(enabled: Boolean, tone: Int) {
        if (!enabled) return
        try {
            val generator = ToneGenerator(AudioManager.STREAM_MUSIC, 70)
            generator.startTone(tone, DURATION_MS)
            android.os.Handler(android.os.Looper.getMainLooper())
                .postDelayed({ generator.release() }, (DURATION_MS + 60).toLong())
        } catch (e: RuntimeException) {
            // Some devices restrict ToneGenerator. Sound is optional, so this
            // is safe to ignore rather than interrupt the game.
            e.printStackTrace()
        }
    }
}
