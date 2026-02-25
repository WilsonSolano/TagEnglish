package com.example.tagenglish.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extensión para crear una sola instancia del DataStore en el Context
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

class AppPreferences(private val context: Context) {

    companion object {
        private val KEY_WORDS_PER_DAY       = intPreferencesKey("words_per_day")
        private val KEY_LAST_GENERATED_DATE = stringPreferencesKey("last_generated_date")
        private val KEY_TODAY_WORD_IDS      = stringPreferencesKey("today_word_ids")   // "1,2,3"
        private val KEY_CURRENT_WEEK        = intPreferencesKey("current_week")

        const val DEFAULT_WORDS_PER_DAY = 5
    }

    // ─── wordsPerDay ──────────────────────────────────────────────────────────

    val wordsPerDay: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_WORDS_PER_DAY] ?: DEFAULT_WORDS_PER_DAY
    }

    suspend fun setWordsPerDay(value: Int) {
        context.dataStore.edit { it[KEY_WORDS_PER_DAY] = value }
    }

    // ─── lastGeneratedDate ────────────────────────────────────────────────────

    val lastGeneratedDate: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_LAST_GENERATED_DATE] ?: ""
    }

    suspend fun setLastGeneratedDate(date: String) {
        context.dataStore.edit { it[KEY_LAST_GENERATED_DATE] = date }
    }

    // ─── todayWordIds ─────────────────────────────────────────────────────────

    /** Devuelve los IDs del día como lista de enteros */
    val todayWordIds: Flow<List<Int>> = context.dataStore.data.map { prefs ->
        val raw = prefs[KEY_TODAY_WORD_IDS] ?: ""
        if (raw.isBlank()) emptyList()
        else raw.split(",").mapNotNull { it.trim().toIntOrNull() }
    }

    suspend fun setTodayWordIds(ids: List<Int>) {
        context.dataStore.edit { it[KEY_TODAY_WORD_IDS] = ids.joinToString(",") }
    }

    // ─── currentWeek ──────────────────────────────────────────────────────────

    val currentWeek: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_CURRENT_WEEK] ?: 1
    }

    suspend fun setCurrentWeek(week: Int) {
        context.dataStore.edit { it[KEY_CURRENT_WEEK] = week }
    }

    suspend fun incrementCurrentWeek() {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_CURRENT_WEEK] ?: 1
            prefs[KEY_CURRENT_WEEK] = current + 1
        }
    }
}
