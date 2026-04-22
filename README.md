# FastVLM-0.5B Android Chat App

A high-performance multimodal chat application featuring **FastVLM-0.5B**, optimized for on-device inference using **LiteRT-LM** and **Qualcomm NPU** acceleration.

## Features
-  **FastVLM-0.5B Integration**: Efficient vision encoding for vision-language tasks.
-  **NPU Acceleration**: Specifically tuned for Snapdragon 8 Elite (S25 Ultra) using Qualcomm NPU runtimes.
-  **Multimodal Inputs**: Supports image + text prompts.
-  **Premium UI**: Modern, sleek design with real-time response streaming.
-  **Backend Switching**: Toggle between CPU, GPU, and NPU on the fly.

## Hardware Requirements
- **Device**: Android device with Snapdragon 8 Elite (recommended for NPU).
- **Minimum SDK**: 31 (Android 12).
- **Architecture**: arm64-v8a only.

## Setup Instructions
1. **Push Model Files**:
   ```bash
   adb shell mkdir -p /data/local/tmp/fastvlm
   adb push FastVLM-0.5B.litertlm /data/local/tmp/fastvlm/
   adb push FastVLM-0.5B.sm8850.litertlm /data/local/tmp/fastvlm/
   ```
2. **Build and Run**:
   Open in Android Studio and deploy to your device.

## License
Subject to the Apple Machine Learning Research Model License Agreement and LiteRT terms.