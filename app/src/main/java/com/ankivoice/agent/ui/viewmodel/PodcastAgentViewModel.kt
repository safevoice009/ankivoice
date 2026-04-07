package com.ankivoice.agent.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ankivoice.agent.ai.PiperEngine
import com.ankivoice.agent.ai.WhisperEngine
import com.ankivoice.agent.data.AnkiNote
import com.ankivoice.agent.data.AnkiRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*

class PodcastAgentViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val ankiRepository = AnkiRepository(application)
    private val whisperEngine = WhisperEngine(application)
    private val piperEngine = PiperEngine(application)

    private val _uiState = MutableStateFlow<StudyUiState>(StudyUiState.Loading)
    val uiState: StateFlow<StudyUiState> = _uiState

    private var currentDeckId: Long = 0L
    private var dueNotes = mutableListOf<AnkiNote>()
    private var currentIndex = 0

    private var sessionSize: Int = 20

    init {
        initializeEngines()
    }

    fun initializeEngines() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = StudyUiState.Loading
            try {
                // Pre-check for required model files in assets
                val assetsList = context.assets.list("models") ?: emptyArray()
                val requiredModels = listOf("ggml-tiny.en.bin", "en_US-lessac-low.onnx")
                val missing = requiredModels.filter { it !in assetsList }

                if (missing.isNotEmpty()) {
                    val errorMsg = "Missing models in assets/models/: ${missing.joinToString(", ")}"
                    android.util.Log.e("PodcastAgentViewModel", errorMsg)
                    _uiState.value = StudyUiState.Error(errorMsg)
                    return@launch
                }

                android.util.Log.d("PodcastAgentViewModel", "Initializing Whisper engine...")
                val wError = whisperEngine.initialize("ggml-tiny.en.bin")
                
                android.util.Log.d("PodcastAgentViewModel", "Initializing Piper engine...")
                val pError = piperEngine.initialize("en_US-lessac-low.onnx")
                
                if (wError == null && pError == null) {
                    android.util.Log.d("PodcastAgentViewModel", "Both engines initialized successfully")
                    _uiState.value = StudyUiState.Ready
                } else {
                    val errorMsg = buildString {
                        append("Init failed. ")
                        if (wError != null) append("STT: $wError. ")
                        if (pError != null) append("TTS: $pError. ")
                    }
                    android.util.Log.e("PodcastAgentViewModel", errorMsg)
                    _uiState.value = StudyUiState.Error(errorMsg)
                }
            } catch (e: Exception) {
                android.util.Log.e("PodcastAgentViewModel", "Critical failure during init", e)
                _uiState.value = StudyUiState.Error("Critical failure: ${e.message}")
            }
        }
    }

    fun configureSession(deckId: Long, size: Int) {
        currentDeckId = deckId
        sessionSize = size
        _uiState.value = StudyUiState.Ready
    }

    fun startSession(deckId: Long) {
        if (_uiState.value is StudyUiState.Error) {
            android.util.Log.e("PodcastAgentViewModel", "Cannot start session: Engines in error state")
            return
        }
        
        currentDeckId = deckId
        dueNotes.clear()
        currentIndex = 0
        
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = StudyUiState.Loading
            android.util.Log.d("PodcastAgentViewModel", "Fetching cards for deck: $deckId")
            val allNotes = ankiRepository.getDueCards(deckId)
            android.util.Log.d("PodcastAgentViewModel", "AnkiRepository returned ${allNotes.size} cards")
            
            dueNotes = allNotes.take(sessionSize).toMutableList()
            currentIndex = 0
            
            if (dueNotes.isNotEmpty()) {
                android.util.Log.d("PodcastAgentViewModel", "Starting study loop with ${dueNotes.size} cards")
                studyLoop()
            } else {
                android.util.Log.w("PodcastAgentViewModel", "No cards to study, finishing session")
                _uiState.value = StudyUiState.Finished
            }
        }
    }

    private suspend fun studyLoop() {
        while (currentIndex < dueNotes.size) {

            val note = dueNotes[currentIndex]
            
            // 1. Transition/Intro
            if (currentIndex == 0) {
                piperEngine.speak("Alright, let's dive into your session. First card coming up.")
                delay(1000)
            } else {
                val fillers = listOf("Moving on...", "Next one.", "Let's see...", "How about this?")
                piperEngine.speak(fillers.random())
                delay(500)
            }

            // 2. Speak Question
            _uiState.value = StudyUiState.SpeakingQuestion(note.front)
            piperEngine.speak(note.front)
            delay(1000)

            // 3. Listen for user thinking/response
            _uiState.value = StudyUiState.Listening
            val transcription = whisperEngine.listen(durationMs = 4000).lowercase()
            
            // 4. Reveal Answer
            _uiState.value = StudyUiState.SpeakingAnswer(note.back)
            piperEngine.speak("The answer is ${note.back}. How did you do?")
            
            // 5. Listen for Grade
            _uiState.value = StudyUiState.Listening
            val gradeText = whisperEngine.listen(durationMs = 3000).lowercase()
            
            val ease = when {
                gradeText.contains("easy") -> 4
                gradeText.contains("good") || gradeText.contains("fine") -> 3
                gradeText.contains("hard") -> 2
                gradeText.contains("again") || gradeText.contains("don't know") -> 1
                else -> 3 // Default to Good
            }

            ankiRepository.answerCard(note.id, ease)
            currentIndex++
            
            delay(1000)
        }
        _uiState.value = StudyUiState.Finished
    }


    override fun onCleared() {
        super.onCleared()
        whisperEngine.release()
        piperEngine.release()
    }
}

sealed class StudyUiState {
    object Loading : StudyUiState()
    object Ready : StudyUiState()
    data class Configuring(val deckId: Long) : StudyUiState()
    data class SpeakingQuestion(val text: String) : StudyUiState()
    object Listening : StudyUiState()
    data class SpeakingAnswer(val text: String) : StudyUiState()
    object Finished : StudyUiState()
    data class Error(val message: String) : StudyUiState()
}
