package com.example.eduapp.helper

import android.content.Context

class AppPreferences(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("eduapp_settings", Context.MODE_PRIVATE)

    var soundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND, true)
        set(value) = prefs.edit().putBoolean(KEY_SOUND, value).apply()

    var shuffleQuestions: Boolean
        get() = prefs.getBoolean(KEY_SHUFFLE, true)
        set(value) = prefs.edit().putBoolean(KEY_SHUFFLE, value).apply()

    companion object {
        private const val KEY_SOUND = "sound_enabled"
        private const val KEY_SHUFFLE = "shuffle_questions"
    }
}