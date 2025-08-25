# HelloWorld Kotlin Android App

A simple Android app built with Kotlin featuring:
- **Hello World** message on the home screen
- **Bottom navigation** with 3 tabs (Home, Dashboard, Notifications)
- **Modern Android architecture** with Navigation Components
- **Latest dependencies** and build tools

## 🚀 Quick Start

### Prerequisites
1. **Java JDK 17+** - [Download here](https://adoptium.net/)
2. **Android Studio** - [Download here](https://developer.android.com/studio)

### System-Wide Environment Setup (One-time)
**Choose your preferred method:**

#### Option 1: Automated Setup (Recommended)
```bash
# Run as Administrator for system-wide setup
.\setup-environment.bat
# OR for PowerShell users:
.\setup-environment.ps1
```

#### Option 2: Manual Environment Variables
Set these system environment variables:
```
ANDROID_HOME=C:\Users\bilva_labs\AppData\Local\Android\Sdk
ANDROID_SDK_ROOT=C:\Users\bilva_labs\AppData\Local\Android\Sdk
```
Add to PATH:
```
%ANDROID_HOME%\platform-tools
%ANDROID_HOME%\tools
%ANDROID_HOME%\tools\bin
```

### Project Setup
1. The setup script creates `local.properties` automatically
2. Or manually copy `local.properties.template` to `local.properties` and update SDK path
3. Open Android Studio
4. Create an Android Virtual Device (AVD):
   - Tools → AVD Manager → Create Virtual Device
   - Choose a device (e.g., Pixel 7)
   - Select system image (API 34 recommended)

### Build & Run
1. **Easy way**: Run `run-app.bat`
2. **Manual way**:
   ```bash
   gradlew.bat clean
   gradlew.bat assembleDebug
   gradlew.bat installDebug
   ```

## 📱 Features

### Home Screen
- Large "Hello World!" message
- Welcome text
- Clean, modern Material Design

### Navigation
- **Home**: Main screen with Hello World
- **Dashboard**: Dashboard content
- **Notifications**: Notifications content

## 🛠️ Technology Stack

- **Language**: Kotlin 1.9.22
- **Build Tool**: Gradle 8.6
- **Android Gradle Plugin**: 8.3.0
- **Target SDK**: 34 (Android 14)
- **Min SDK**: 24 (Android 7.0)
- **Java Version**: 17

### Key Dependencies
- AndroidX Core KTX 1.12.0
- Material Design 1.11.0
- Navigation Components 2.7.6
- Jetpack Compose 2024.02.00 (enabled)
- Lifecycle ViewModels 2.7.0

## 📁 Project Structure

```
app/
├── src/main/
│   ├── java/com/example/helloworldapp/
│   │   ├── MainActivity.kt
│   │   └── ui/
│   │       ├── home/HomeFragment.kt & HomeViewModel.kt
│   │       ├── dashboard/DashboardFragment.kt & DashboardViewModel.kt
│   │       └── notifications/NotificationsFragment.kt & NotificationsViewModel.kt
│   └── res/
│       ├── layout/ (XML layouts)
│       ├── navigation/ (Navigation graph)
│       ├── menu/ (Bottom navigation menu)
│       └── values/ (Strings, colors, themes)
```

## 🔧 Build Configuration

- **Gradle**: 8.6 (latest stable)
- **Java**: 17 (LTS)
- **ViewBinding**: Enabled
- **Jetpack Compose**: Ready (for future enhancements)
- **Build optimizations**: Parallel builds, configuration cache enabled

## 🎯 Running in AVD

1. Start Android Studio
2. Launch your AVD: Tools → AVD Manager → Play button
3. Run `gradlew.bat installDebug` or use `run-app.bat`
4. Find "HelloWorldApp" in the emulator and tap to open

## 📝 Development Notes

- Uses modern Android architecture with MVVM pattern
- ViewBinding for type-safe view references
- Navigation Component for fragment management
- Material Design 3 theming
- Ready for Jetpack Compose integration

## 🚨 Troubleshooting

**Build fails?**
- Check Java version: `java -version`
- Verify ANDROID_HOME is set
- Run `setup.bat` for diagnostics

**Can't find AVD?**
- Open Android Studio → Tools → AVD Manager
- Create new virtual device if none exist

**App doesn't install?**
- Make sure AVD is running
- Check `adb devices` to see connected devices
- Try `gradlew.bat clean` then rebuild

---

**Built with ❤️ using the latest Android development tools**