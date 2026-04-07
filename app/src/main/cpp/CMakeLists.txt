cmake_minimum_required(VERSION 3.22.1)

project("ankivoice")

set(CMAKE_CXX_STANDARD 17)

# Include directories
include_directories(
    .
    include
    src
    ggml/include
    ggml/src
    ggml/src/ggml-cpu
)

# Compile flags
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -O3")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O3")

# GGML Sources
set(GGML_SOURCES
    ggml/src/ggml.c
    ggml/src/ggml-alloc.c
    ggml/src/ggml-backend.cpp
    ggml/src/ggml-backend-reg.cpp
    ggml/src/ggml-backend-dl.cpp
    ggml/src/ggml-quants.c
    ggml/src/gguf.cpp
    ggml/src/ggml-opt.cpp
    ggml/src/ggml.cpp
    ggml/src/ggml-threading.cpp
    
    ggml/src/ggml-cpu/ggml-cpu.c
    ggml/src/ggml-cpu/ggml-cpu.cpp
    ggml/src/ggml-cpu/ops.cpp
    ggml/src/ggml-cpu/unary-ops.cpp
    ggml/src/ggml-cpu/binary-ops.cpp
    ggml/src/ggml-cpu/vec.cpp
    ggml/src/ggml-cpu/traits.cpp
    
    # Missing essentials for new ggml structure
    ggml/src/ggml-cpu/quants.c
    ggml/src/ggml-cpu/repack.cpp
    
    # ARM-specific implementations (Android)
    ggml/src/ggml-cpu/arch/arm/quants.c
    ggml/src/ggml-cpu/arch/arm/repack.cpp
    ggml/src/ggml-cpu/arch/arm/cpu-feats.cpp
)

# Whisper Sources
set(WHISPER_SOURCES
    src/whisper.cpp
    whisper-jni.cpp
)

add_library(ankivoice SHARED
    ${GGML_SOURCES}
    ${WHISPER_SOURCES}
)

target_compile_definitions(ankivoice PRIVATE
    GGML_BUILD_NUMBER=1
    GGML_USE_CPU
    WHISPER_BUILD_NUMBER=1
)

target_include_directories(ankivoice PRIVATE
    .
    include
    src
    ggml/include
    ggml/src
    ggml/src/ggml-cpu
)

target_link_libraries(ankivoice
    log
    android
    dl
)
