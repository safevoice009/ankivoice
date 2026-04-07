package com.ankivoice.agent.ai

import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream

class WhisperEngine(private val context: Context) {

    companion object {
        private var isLibraryLoaded = false
        fun loadLibrary() {
            if (!isLibraryLoaded) {
                try {
                    System.loadLibrary("ankivoice")
                    isLibraryLoaded = true
                    Log.d("WhisperEngine", "Native library 'ankivoice' loaded successfully")
                } catch (e: Exception) {
                    Log.e("WhisperEngine", "Failed to load native library: ${e.message}")
                }
            }
        }
    }

    private val audioRecorder = AudioRecorder()
    private var nativeContext: Long = 0L
    private val scope = CoroutineScope(Dispatchers.Default + Job())

    init {
        loadLibrary()
    }

    fun initialize(modelName: String): String? {
        Log.d("WhisperEngine", "Starting initialization for model: $modelName")
        return try {
            val modelFile = getModelFile(modelName)
            if (!modelFile.exists()) {
                Log.e("WhisperEngine", "Model file not found at: ${modelFile.absolutePath}")
                return "Model file missing: $modelName"
            }
            
            Log.d("WhisperEngine", "Found model: ${modelFile.absolutePath} (${modelFile.length()} bytes)")
            nativeContext = initContext(modelFile.absolutePath)
            
            if (nativeContext == 0L) {
                Log.e("WhisperEngine", "Native context failed to initialize")
                return "Native init failed (0 context)"
            }
            
            Log.d("WhisperEngine", "Successfully initialized context: $nativeContext")
            null // Success
        } catch (e: Exception) {
            Log.e("WhisperEngine", "Init exception: ${e.message}", e)
            "Exception: ${e.message}"
        }
    }

    /**
     * Records audio for [durationMs] and returns the transcription.
     */
    suspend fun listen(durationMs: Long = 3000): String = withContext(Dispatchers.IO) {
        if (nativeContext == 0L) return@withContext ""
        
        val accumulatedAudio = mutableListOf<Float>()
        
        audioRecorder.startRecording { data ->
            accumulatedAudio.addAll(data.toList())
        }
        
        delay(durationMs)
        audioRecorder.stopRecording()
        
        if (accumulatedAudio.isEmpty()) return@withContext ""
        
        return@withContext fullTranscribe(nativeContext, accumulatedAudio.toFloatArray())
    }

    fun transcribe(audioData: FloatArray): String {
        if (nativeContext == 0L) return ""
        return fullTranscribe(nativeContext, audioData)
    }

    fun release() {
        if (nativeContext != 0L) {
            freeContext(nativeContext)
            nativeContext = 0L
        }
    }

    private fun getModelFile(fileName: String): File {
        val file = File(context.filesDir, fileName)
        if (!file.exists()) {
            context.assets.open("models/$fileName").use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
        }
        return file
    }

    // Native declarations
    private external fun initContext(modelPath: String): Long
    private external fun fullTranscribe(contextPtr: Long, audioData: FloatArray): String
    private external fun freeContext(contextPtr: Long)
}
