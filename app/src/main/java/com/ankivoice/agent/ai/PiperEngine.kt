package com.ankivoice.agent.ai

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import io.github.givimad.piperjni.PiperJNI
import io.github.givimad.piperjni.PiperVoice
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Paths
import java.nio.file.Path

class PiperEngine(private val context: Context) {

    private var piper: PiperJNI? = null
    private var voice: PiperVoice? = null
    private var audioTrack: AudioTrack? = null

    fun release() {
        voice = null
        piper = null
        audioTrack?.stop()
        audioTrack?.release()
        audioTrack = null
    }

    fun initialize(modelName: String): String? {
        Log.d("PiperEngine", "Starting initialization for model: $modelName")
        if (piper != null && voice != null) return null
        
        return try {
            if (piper == null) {
                piper = PiperJNI()
                val jniOk = piper?.initialize(false) ?: false
                if (jniOk == false) {
                    Log.e("PiperEngine", "PiperJNI native init failed")
                    return "JNI init failed"
                }
            }
            
            val p = piper ?: return "Piper instance null"
            val modelFile = getModelFile(modelName)
            val configFile = getModelFile("$modelName.json")
            
            if (!modelFile.exists()) return "Model file missing: $modelName"
            if (!configFile.exists()) return "Config file missing: $modelName.json"

            // Log file info for debugging
            Log.d("PiperEngine", "Loading model: ${modelFile.absolutePath} (${modelFile.length()} bytes)")
            Log.d("PiperEngine", "Loading config: ${configFile.absolutePath} (${configFile.length()} bytes)")

            // Correct Piper JNI 1.4.1 usage: Path based method and long speakerId
            voice = p.loadVoice(modelFile.toPath(), configFile.toPath(), 0L)
            
            if (voice == null) {
                Log.e("PiperEngine", "Failed to load voice context")
                return "Voice load failed (null context)"
            }
            
            Log.d("PiperEngine", "Successfully initialized Piper voice")
            null // Success
        } catch (e: Exception) {
            Log.e("PiperEngine", "Piper init exception: ${e.message}", e)
            "Exception: ${e.message}"
        }
    }

    fun speak(text: String) {
        val p = piper ?: return
        val v = voice ?: return
        
        Log.d("PiperEngine", "Synthesizing text: $text")
        try {
            val audioData = p.textToAudio(v, text)
            
            if (audioData != null && audioData.isNotEmpty()) {
                Log.d("PiperEngine", "Synthesis successful, playing ${audioData.size} samples")
                playAudio(audioData)
            } else {
                Log.w("PiperEngine", "Synthesis returned empty audio data")
            }
        } catch (e: Exception) {
            Log.e("PiperEngine", "Synthesis failed: ${e.message}")
        }
    }

    private fun playAudio(audioData: ShortArray) {
        val sampleRate = 22050
        
        try {
            if (audioTrack == null) {
                val minBufferSize = AudioTrack.getMinBufferSize(
                    sampleRate, 
                    AudioFormat.CHANNEL_OUT_MONO, 
                    AudioFormat.ENCODING_PCM_16BIT
                )
                audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(minBufferSize * 4)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
            }

            audioTrack?.let { track ->
                if (track.playState != AudioTrack.PLAYSTATE_PLAYING) {
                    track.play()
                }
                track.write(audioData, 0, audioData.size)
            }
        } catch (e: Exception) {
            Log.e("PiperEngine", "AudioTrack failed: ${e.message}")
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
}
