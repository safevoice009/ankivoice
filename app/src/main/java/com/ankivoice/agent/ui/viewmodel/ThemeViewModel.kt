package com.ankivoice.agent.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class ThemeViewModel : ViewModel() {
    val isDarkMode = mutableStateOf(false) // Default to LIGHT mode

    fun toggleTheme() {
        isDarkMode.value = !isDarkMode.value
    }
}
