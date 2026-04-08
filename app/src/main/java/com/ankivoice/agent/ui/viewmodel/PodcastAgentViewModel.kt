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

    private val _uiState = MutableStateFlow<StudyUiState>(StudyUiState.Loading("STARTING..."))
    val uiState: StateFlow<StudyUiState> = _uiState

    private val _syncEvent = MutableStateFlow<String?>(null)
    val syncEvent: StateFlow<String?> = _syncEvent

    private var currentDeckId: Long = 0L
    private var dueNotes = mutableListOf<AnkiNote>()
    private var currentIndex = 0

    private var sessionSize: Int = 20

    init {
        initializeEngines()
    }

    fun initializeEngines() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = StudyUiState.Loading("CHECKING MODELS...")
            try {
                val assetsList = context.assets.list("models") ?: emptyArray()
                val requiredModels = listOf("ggml-tiny.en.bin", "en_US-lessac-low.onnx")
                val missing = requiredModels.filter { it !in assetsList }

                if (missing.isNotEmpty()) {
                    val errorMsg = "Missing models in assets/models/: ${missing.joinToString(", ")}"
                    android.util.Log.e("PodcastAgentViewModel", errorMsg)
                    _uiState.value = StudyUiState.Error(errorMsg)
                    return@launch
                }

                _uiState.value = StudyUiState.Loading("INITIALIZING WHISPER...")
                val wError = whisperEngine.initialize("ggml-tiny.en.bin")
                
                _uiState.value = StudyUiState.Loading("INITIALIZING PIPER...")
                val pError = piperEngine.initialize("en_US-lessac-low.onnx")
                
                if (wError == null && pError == null) {
                    _uiState.value = StudyUiState.Ready
                } else {
                    val errorMsg = "Init failed. STT: $wError, TTS: $pError"
                    _uiState.value = StudyUiState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _uiState.value = StudyUiState.Error("Critical failure: ${e.message}")
            }
        }
    }

    fun configureSession(deckId: Long, size: Int) {
        currentDeckId = deckId
        sessionSize = size
        _uiState.value = StudyUiState.Ready
    }

    fun setVoiceSpeed(speed: Float) {
        piperEngine.setSpeed(speed)
    }

    fun syncWithAnkiDroid() {
        viewModelScope.launch(Dispatchers.IO) {
            _syncEvent.value = null
            ankiRepository.getDeckList() 
            _syncEvent.value = "SYNC COMPLETE"
            delay(2000)
            _syncEvent.value = null
        }
    }

    fun startSession(deckId: Long) {
        if (_uiState.value is StudyUiState.Error) return
        
        currentDeckId = deckId
        dueNotes.clear()
        currentIndex = 0
        
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = StudyUiState.Loading("FETCHING CARDS...")
            val allNotes = ankiRepository.getDueCards(deckId)
            dueNotes = allNotes.take(sessionSize).toMutableList()
            currentIndex = 0
            
            if (dueNotes.isNotEmpty()) {
                studyLoop()
            } else {
                _uiState.value = StudyUiState.Finished
            }
        }
    }

    private suspend fun studyLoop() {
        while (currentIndex < dueNotes.size) {
            val note = dueNotes[currentIndex]
            
            // 1. Transition/Intro
            if (currentIndex == 0) {
                piperEngine.speak("Alright, let's dive into your session.")
                delay(2000)
            } else {
                val fillers = listOf("Moving on...", "Next one.", "Let's see...", "How about this?")
                piperEngine.speak(fillers.random())
                delay(1500)
            }

            // 2. Speak Question
            _uiState.value = StudyUiState.SpeakingQuestion(note.front)
            piperEngine.speak(note.front)
            delay(2000 + (note.front.length * 40L)) 

            // 3. Listen for user response
            _uiState.value = StudyUiState.Listening
            whisperEngine.listen(durationMs = 4000)
            
            // 4. Reveal Answer
            _uiState.value = StudyUiState.Transcribing
            delay(800) 
            
            _uiState.value = StudyUiState.SpeakingAnswer(note.back)
            piperEngine.speak("The answer is ${note.back}.")
            delay(2000 + (note.back.length * 40L))
            
            // 5. Ask for Grade
            _uiState.value = StudyUiState.Listening
            val gradeAudio = whisperEngine.listen(durationMs = 3000)
            
            _uiState.value = StudyUiState.Transcribing
            val gradeText = gradeAudio.lowercase()
            
            val ease = when {
                gradeText.contains("easy") -> 4
                gradeText.contains("good") || gradeText.contains("fine") -> 3
                gradeText.contains("hard") -> 2
                gradeText.contains("again") || gradeText.contains("don't know") -> 1
                else -> 3 
            }

            ankiRepository.answerCard(note.id, ease)
            currentIndex++
            delay(500)
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
    data class Loading(val message: String = "INITIALIZING AI AGENT...") : StudyUiState()
    object Ready : StudyUiState()
    data class Configuring(val deckId: Long) : StudyUiState()
    data class SpeakingQuestion(val text: String) : StudyUiState()
    object Listening : StudyUiState()
    object Transcribing : StudyUiState()
    data class SpeakingAnswer(val text: String) : StudyUiState()
    object Finished : StudyUiState()
    data class Error(val message: String) : StudyUiState()
}
