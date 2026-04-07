<!-- 
  SEO METADATA for AnkiVoice
  Title: AnkiVoice - The AI Voice Agent for Anki Flashcards
  Description: Open-source, hands-free Anki study agent with local AI Whisper STT and Piper TTS.
  Keywords: Anki, AnkiDroid, Voice Agent, AI Study, Hands-free, Active Recall, Whisper.cpp, Piper TTS
-->

# <p align="center"><img src="docs/assets/ankivoice_hybrid.png" width="180"><br>AnkiVoice: The Elite AI Voice Agent for Anki</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-00C853?style=for-the-badge&logo=android&logoColor=white" alt="Platform">
  <img src="https://img.shields.io/badge/AI_Engine-Local_Whisper-7B1FA2?style=for-the-badge&logo=openai" alt="AI Engine">
  <img src="https://img.shields.io/badge/License-MIT-007ACC?style=for-the-badge" alt="License">
  <img src="https://img.shields.io/badge/Build-v1.1.0--alpha-FF6F00?style=for-the-badge" alt="Version">
</p>

---

## 🎙️ Evolution of Active Recall
**AnkiVoice** isn't just an audio companion; it's a world-class **Autonomous Study Agent**. By leveraging cutting-edge, on-device AI, AnkiVoice transforms your static flashcards into a dynamic, two-way conversation. Stop staring at screens and start *mastering* your knowledge while walking, driving, or training.

### 🌟 Why AnkiVoice is World-Class:
- **Zero-Latency Voice Interaction**: Powered by a highly optimized **Whisper.cpp** engine for instant speech-to-text.
- **Neural TTS Personalities**: Ultra-realistic local speech synthesis via **Piper**, making your study sessions feel human.
- **Privacy First, Second, and Always**: Your knowledge is your own. No cloud processing, no data leaks, no subscriptions.
- **Intelligent Deck Synergy**: Seamlessly bridges with **AnkiDroid**, respecting your intervals and review schedules.

---

## 🔥 Features that WOW
- 🧠 **Dynamic Dialogue Parsing**: Automatically converts card metadata into natural, engaging speech patterns.
- 🗣️ **Hands-Free Grading**: Simply say "Good," "Easy," or "Again." The agent handles the rest.
- 🌑 **Premium Glassmorphic UI**: A high-fidelity, interactive interface designed for modern Android enthusiasts.
- ⚡ **Local-First Performance**: Optimized for low-power devices without sacrificing accuracy or speed.

---

## 🚀 Experience the Future
### 1. Installation
Download the latest pre-release from the [Releases](https://github.com/safevoice009/podcards-ai-engine/releases) tab.

### 2. High-Performance Setup
To enable the full AI experience, place your neural models in the secure local directory:
1.  **Whisper (STT)**: Download [ggml-tiny.en.bin](https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-tiny.en.bin)
2.  **Piper (TTS)**: Download [en_US-lessac-low.onnx](https://huggingface.co/rhasspy/piper-voices/resolve/v1.0.0/en/en_US/lessac/low/en_US-lessac-low.onnx)
3.  **Path**: `/Android/data/com.ankivoice.agent/files/models/`

---

## 🗺️ The World-Class Roadmap
- [ ] **Adaptive AI Personalities**: Let "AnkiVoice" change its tone based on your study performance.
- [ ] **Multi-Lingual Mastery**: Support for Spanish, French, Japanese, and Mandarin voice-engines.
- [ ] **Voice-First Note Creation**: dictating new cards directly into your Anki decks.

---

## 🛠️ Technical Interior
- **Core Logic**: Kotlin Coroutines & Flow for asynchronous AI processing.
- **UI Engine**: Jetpack Compose with custom glassmorphic shaders.
- **AI Runtimes**: JNI-bridged C++ Whisper and ONNX Runtime for Piper.

---

<p align="center">
  <b>Built for the Power User. Dedicated to the Community.</b><br>
  Developed with ❤️ by <a href="https://github.com/safevoice009">SafeVoice009</a>
</p>

<p align="center">
  <img src="https://img.shields.io/github/stars/safevoice009/podcards-ai-engine?style=social" alt="Stars">
  <img src="https://img.shields.io/github/forks/safevoice009/podcards-ai-engine?style=social" alt="Forks">
</p>
