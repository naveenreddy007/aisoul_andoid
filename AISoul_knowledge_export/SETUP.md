# AI Soul - Quick Setup Guide

## Project Structure Overview

```
ai-soul/
├── app/
│   ├── src/main/java/com/aisoul/
│   │   ├── MainActivity.kt
│   │   ├── AISoulApplication.kt
│   │   ├── data/
│   │   │   ├── entities.kt
│   │   │   ├── DAOs.kt
│   │   │   └── AppDatabase.kt
│   │   ├── ai/
│   │   │   └── ModelManager.kt
│   │   ├── services/
│   │   │   ├── NotificationListener.kt
│   │   │   ├── SMSReceiver.kt
│   │   │   └── AIProcessingService.kt
│   │   ├── ui/
│   │   │   ├── theme/
│   │   │   │   ├── Color.kt
│   │   │   │   ├── Type.kt
│   │   │   │   └── Theme.kt
│   │   │   └── components/
│   │   │       ├── Dashboard.kt
│   │   │       ├── PrivacySettings.kt
│   │   │       └── VoiceCommand.kt
│   │   └── utils/
│   │       ├── DataStoreManager.kt
│   │       ├── UsageStatsCollector.kt
│   │       └── VectorDatabaseHelper.kt
│   ├── src/main/res/
│   │   ├── values/strings.xml
│   │   └── drawable/ic_notification.xml
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── build.gradle.kts
├── settings.gradle.kts
├── gradlew.bat
├── README.md
└── SETUP.md
```

## Quick Start

### 1. Prerequisites
- Android Studio Arctic Fox or later
- JDK 11 or later
- Android SDK 34
- Physical Android device (API 26+)

### 2. Clone and Open
```bash
git clone <repository-url>
cd ai-soul
```

Open in Android Studio: File → Open → Select `ai-soul` folder

### 3. Download AI Models
1. Download Gemma 2B model from TensorFlow Hub
2. Create directory: `app/src/main/assets/models/`
3. Place model files in the models directory
4. Update `ModelManager.kt` with correct model paths

### 4. Build and Run
```bash
# Build the project
./gradlew build

# Install on connected device
./gradlew installDebug
```

### 5. Grant Permissions
On first launch, grant the following permissions:
- SMS access
- Notification access
- Usage stats access
- Biometric authentication (optional)

## Development Phases Status

### ✅ Phase 1: Foundation (Complete)
- [x] Basic project structure
- [x] TensorFlow Lite integration
- [x] Notification listener
- [x] SMS receiver
- [x] Local storage with Room
- [x] Basic UI with Jetpack Compose
- [x] Privacy settings
- [x] Voice command interface

### 🚧 Phase 2: Intelligence (Next)
- [ ] Advanced AI insights
- [ ] Voice commands
- [ ] Task automation
- [ ] Usage pattern analysis
- [ ] Model fine-tuning

### 📋 Phase 3: Enhancement
- [ ] Custom model training
- [ ] Widget support
- [ ] Advanced privacy controls
- [ ] Performance optimizations

## Testing

### Unit Tests
```bash
./gradlew test
```

### Integration Tests
```bash
./gradlew connectedAndroidTest
```

### Manual Testing Checklist
- [ ] Notification processing
- [ ] SMS processing
- [ ] Usage stats collection
- [ ] AI insights generation
- [ ] Privacy settings
- [ ] Voice commands
- [ ] Data encryption
- [ ] Biometric authentication

## Troubleshooting

### Common Issues

1. **Build fails with TensorFlow Lite**
   - Ensure you have the correct NDK version
   - Check model file paths in `ModelManager.kt`

2. **Permissions not working**
   - Go to Settings → Apps → AI Soul → Permissions
   - Enable all required permissions
   - Restart the app

3. **Database migration issues**
   - Clear app data and reinstall
   - Check Room database version

4. **Vector database not initializing**
   - Ensure proper SQLite setup
   - Check FTS table creation

## Performance Benchmarks

### Target Metrics
- App startup: < 2 seconds
- Model loading: < 3 seconds
- Inference time: < 100ms
- Memory usage: < 200MB
- Battery impact: < 2%

### Monitoring
Use Android Studio Profiler to monitor:
- CPU usage
- Memory allocations
- Network activity (should be 0)
- Battery usage

## Security Checklist

- [ ] All data encrypted at rest
- [ ] No network calls
- [ ] Biometric authentication enabled
- [ ] Secure random keys generated
- [ ] Data export encrypted
- [ ] No sensitive data in logs

## Next Steps

1. **Download models** and place in assets/models/
2. **Test on real device** with various Android versions
3. **Implement Phase 2 features** (voice commands, AI insights)
4. **Add comprehensive tests** for all components
5. **Performance optimization** based on profiling
6. **User testing** for UI/UX improvements

## Support

For issues or questions:
- Check the troubleshooting section above
- Review the full README.md
- Check Android Studio logs
- Ensure all permissions are granted

The project is now ready for development and testing! 🚀