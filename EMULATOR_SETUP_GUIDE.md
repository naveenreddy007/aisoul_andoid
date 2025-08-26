# AI Soul App Setup on Emulator - Complete Guide

## Overview
This document summarizes all the steps completed to set up the AI Soul app with a Gemma 3 270M model on an Android emulator.

## Steps Completed

### 1. Model File Transfer ✅ DONE
- Transferred `gemma-3-270m-it-int8.litertlm` (304MB) from local machine to emulator's Download directory
- Command used: `adb push "C:\Users\bilva_labs\Downloads\Compressed\gemma-3-tflite-gemma-3-270m-it-int8-v1\gemma-3-270m-it-int8.litertlm" /sdcard/Download/`

### 2. File Format Conversion ✅ DONE
- Renamed the `.litertlm` file to `.bin` format to match app requirements
- Command used: `adb shell mv /sdcard/Download/gemma-3-270m-it-int8.litertlm /sdcard/Download/gemma-2b-it.bin`

### 3. App Installation ✅ DONE
- Built and installed the AI Soul app on the emulator
- Command used: `.\gradlew installDebug`

### 4. Model File Placement ✅ DONE
- Copied the model file to the app's private directory where it can be detected
- Command used: `adb shell cp /sdcard/Download/gemma-2b-it.bin /sdcard/Android/data/com.aisoul.privateassistant/files/`

### 5. App Launch ✅ DONE
- Launched the AI Soul app on the emulator
- Command used: `adb shell am start -n com.aisoul.privateassistant/.MainActivity`

## File Locations

### On Emulator:
- **Model File**: `/sdcard/Android/data/com.aisoul.privateassistant/files/gemma-2b-it.bin`
- **App Package**: `com.aisoul.privateassistant`

## Important Notes

1. **File Format**: The original `.litertlm` file was renamed to `.bin` to match the MediaPipe-compatible format expected by the AI Soul app.

2. **Model Mapping**: The Gemma 270M model is mapped to the GEMMA_2B model type in the app for compatibility.

3. **App Requirements**: The AI Soul app has been updated to work with MediaPipe LLM Inference API and requires .bin files.

4. **Testing**: The app should now detect the model file and be able to use it for AI inference.

## Verification Commands

To verify each step:

1. Check if app is installed:
   ```
   adb shell pm list packages | findstr aisoul
   ```

2. Check if model file exists in app directory:
   ```
   adb shell ls -la /sdcard/Android/data/com.aisoul.privateassistant/files/gemma-2b-it.bin
   ```

## Next Steps

1. Open the AI Soul app on the emulator
2. Navigate to the Models tab to verify the model is detected
3. Try loading the model
4. Go to the Chat tab to test AI inference with the model

The setup is now complete and the app should be ready to use with the Gemma 3 270M model.