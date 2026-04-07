package com.ankivoice.agent.domain

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.text.SimpleDateFormat
import java.util.*

class UsageManager(private val context: Context) {

    private val DAILY_LIMIT = 20
    private val PREFS_NAME = "usage_prefs"
    private val REVIEW_COUNT_KEY = "review_count"
    private val LAST_RESET_DATE_KEY = "last_reset_date"
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _remainingReviews = MutableStateFlow(calculateRemaining())
    val remainingReviews: Flow<Int> = _remainingReviews

    private fun calculateRemaining(): Int {
        val lastResetDate = prefs.getString(LAST_RESET_DATE_KEY, "") ?: ""
        val today = getTodayDate()
        
        return if (lastResetDate != today) {
            DAILY_LIMIT
        } else {
            val count = prefs.getInt(REVIEW_COUNT_KEY, 0)
            (DAILY_LIMIT - count).coerceAtLeast(0)
        }
    }

    suspend fun incrementUsage() {
        val lastResetDate = prefs.getString(LAST_RESET_DATE_KEY, "") ?: ""
        val today = getTodayDate()
        
        val editor = prefs.edit()
        if (lastResetDate != today) {
            editor.putString(LAST_RESET_DATE_KEY, today)
            editor.putInt(REVIEW_COUNT_KEY, 1)
        } else {
            val currentCount = prefs.getInt(REVIEW_COUNT_KEY, 0)
            editor.putInt(REVIEW_COUNT_KEY, currentCount + 1)
        }
        editor.apply()
        _remainingReviews.value = calculateRemaining()
    }

    private fun getTodayDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
}
