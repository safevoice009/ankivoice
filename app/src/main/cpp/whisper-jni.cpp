#include <jni.h>
#include <string>
#include <vector>
#include "whisper.h"
#include <android/log.h>

#define LOG_TAG "WhisperJNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C" JNIEXPORT jlong JNICALL
Java_com_ankivoice_agent_ai_WhisperEngine_initContext(
        JNIEnv* env,
        jobject /* this */,
        jstring modelPath) {
    
    const char* path = env->GetStringUTFChars(modelPath, nullptr);
    struct whisper_context_params params = whisper_context_default_params();
    
    struct whisper_context* ctx = whisper_init_from_file_with_params(path, params);
    env->ReleaseStringUTFChars(modelPath, path);
    
    if (ctx == nullptr) {
        LOGE("Failed to initialize whisper context");
        return 0;
    }
    
    return reinterpret_cast<jlong>(ctx);
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_ankivoice_agent_ai_WhisperEngine_fullTranscribe(
        JNIEnv* env,
        jobject /* this */,
        jlong contextPtr,
        jfloatArray audioData) {
    
    auto* ctx = reinterpret_cast<struct whisper_context*>(contextPtr);
    if (ctx == nullptr) return env->NewStringUTF("");

    jsize len = env->GetArrayLength(audioData);
    float* pAudio = env->GetFloatArrayElements(audioData, nullptr);
    
    std::vector<float> pcm(pAudio, pAudio + len);
    env->ReleaseFloatArrayElements(audioData, pAudio, JNI_ABORT);

    struct whisper_full_params params = whisper_full_default_params(WHISPER_SAMPLING_GREEDY);
    params.print_realtime = false;
    params.print_progress = false;
    params.n_threads = 4; // Adjust based on device

    if (whisper_full(ctx, params, pcm.data(), pcm.size()) != 0) {
        LOGE("Failed to process audio");
        return env->NewStringUTF("");
    }

    std::string result = "";
    int n_segments = whisper_full_n_segments(ctx);
    for (int i = 0; i < n_segments; ++i) {
        const char* text = whisper_full_get_segment_text(ctx, i);
        result += text;
    }

    return env->NewStringUTF(result.c_str());
}

extern "C" JNIEXPORT void JNICALL
Java_com_ankivoice_agent_ai_WhisperEngine_freeContext(
        JNIEnv* env,
        jobject /* this */,
        jlong contextPtr) {
    
    auto* ctx = reinterpret_cast<struct whisper_context*>(contextPtr);
    if (ctx != nullptr) {
        whisper_free(ctx);
    }
}
