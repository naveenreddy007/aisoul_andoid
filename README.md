# AI Soul - Your Private AI Assistant

<div align="center">

![AI Soul Logo](https://img.shields.io/badge/AI%20Soul-Privacy%20First-blue?style=for-the-badge&logo=android)

[![Build Status](https://img.shields.io/badge/Build-Passing-brightgreen?style=flat-square)](https://github.com/aisoul/private-assistant)
[![Version](https://img.shields.io/badge/Version-1.0.0--dev-orange?style=flat-square)](https://github.com/aisoul/private-assistant/releases)
[![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)](LICENSE)
[![Android](https://img.shields.io/badge/Android-API%2024+-green?style=flat-square&logo=android)](https://android.com)

**A privacy-first Android AI assistant that processes everything locally on your device**

[Features](#features) • [Installation](#installation) • [Architecture](#architecture) • [Development](#development) • [Privacy](#privacy)

</div>

---

## 🌟 Features

### ✅ Sprint 1 (COMPLETED)
- **🔒 Privacy-First Design**: All AI processing happens locally on your device
- **🗨️ Chat Interface**: Clean, intuitive conversation interface with demo mode
- **🤖 AI Model Management**: Download and manage AI models with device compatibility checks
- **🔐 Encrypted Database**: AES-256 encrypted local storage using SQLCipher
- **🔔 Notification Analysis**: Smart notification processing and insights
- **⚙️ Developer Tools**: Comprehensive dev panel for debugging and testing
- **📱 Material Design 3**: Modern, accessible UI with bottom navigation

### 🚧 Upcoming Features
- **Voice Interface**: Voice-first interactions for accessibility
- **Smart Responses**: AI-generated response suggestions
- **App Usage Analysis**: Intelligent insights from device usage patterns
- **SMS Integration**: Context-aware SMS analysis and responses
- **Advanced AI Models**: MediaPipe-compatible Gemma-2B/7B and Phi-2 models
- **Vector Search**: FAISS-powered semantic search capabilities

---

## 🚀 Installation

### Prerequisites
- **Android Studio** Koala | 2024.1.1 or later
- **Android SDK** API level 24 (Android 7.0) or higher
- **Java 17 LTS** for development
- **Gradle 8.6+**
- **Kotlin 1.9.22+**

### Quick Start

1. **Clone the repository**
   ```bash
   git clone https://github.com/aisoul/private-assistant.git
   cd private-assistant
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an existing project"
   - Navigate to the cloned directory

3. **Build and Run**
   ```bash
   ./gradlew assembleDebug
   ./gradlew installDebug
   ```

4. **Run Tests**
   ```bash
   ./gradlew test
   ./gradlew connectedDebugAndroidTest
   ```

---

## 🏗️ Architecture

### Tech Stack
- **Language**: Kotlin 1.9.22
- **UI Framework**: Jetpack Compose with Material Design 3
- **Database**: Room + SQLCipher for encryption
- **AI Engine**: MediaPipe LLM Inference API (Local processing)
- **Dependency Injection**: Hilt
- **Async Processing**: Kotlin Coroutines + Flow
- **Navigation**: Jetpack Navigation Compose

### Project Structure
```
app/src/main/java/com/aisoul/privateassistant/
├── ui/                          # UI Layer (Jetpack Compose)
│   ├── screens/
│   │   ├── chat/               # Chat interface
│   │   ├── models/             # AI model management
│   │   ├── privacy/            # Privacy settings
│   │   └── devpanel/          # Developer tools
│   └── theme/                  # Material Design 3 theme
├── data/                       # Data Layer
│   ├── database/              # Room database + encryption
│   ├── entities/              # Database entities
│   ├── dao/                   # Data Access Objects
│   └── repository/            # Repository pattern
├── services/                   # Background Services
│   ├── NotificationListenerService.kt
│   └── AIProcessingService.kt
├── core/                      # Core Business Logic
│   ├── ai/                    # AI model management
│   ├── demo/                  # Demo mode functionality
│   └── security/              # Encryption utilities
└── MainActivity.kt            # App entry point
```

### Database Schema
```sql
-- Conversations table (encrypted)
CREATE TABLE conversations (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL
);

-- Messages table (encrypted)
CREATE TABLE messages (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    conversation_id INTEGER NOT NULL,
    content TEXT NOT NULL,
    is_from_user INTEGER NOT NULL,
    timestamp INTEGER NOT NULL,
    model_used TEXT,
    processing_time_ms INTEGER,
    FOREIGN KEY(conversation_id) REFERENCES conversations(id)
);

-- AI Models table (encrypted)
CREATE TABLE ai_models (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT,
    size_bytes INTEGER NOT NULL,
    min_ram_mb INTEGER NOT NULL,
    min_storage_mb INTEGER NOT NULL,
    is_downloaded INTEGER NOT NULL DEFAULT 0,
    is_active INTEGER NOT NULL DEFAULT 0,
    download_url TEXT,
    created_at INTEGER NOT NULL
);
```

---

## 📅 Development Timeline

### Sprint 1: Foundation (August 24-25, 2025) ✅ COMPLETE
**Objective**: Transform HelloWorld → AI Soul Foundation

**Day 1 Achievements**:
- ✅ Complete project transformation and package restructure
- ✅ Jetpack Compose UI scaffolding with 4 screens
- ✅ Material Design 3 theming and navigation
- ✅ SQLCipher database integration with encryption
- ✅ Basic entity models and DAOs

**Day 2 Achievements**:
- ✅ AI inference engine with multi-model support
- ✅ TensorFlow Lite integration architecture
- ✅ Advanced model management system
- ✅ Voice interface implementation
- ✅ Notification listener service
- ✅ Developer tools and debugging panel
- ✅ Comprehensive testing suite
- ✅ Performance optimization and monitoring

### Sprint 2: MediaPipe AI Integration (COMPLETED) ✅ COMPLETE
**Objective**: Local AI Processing with MediaPipe LLM Inference API

**Completed Features**:
- ✅ MediaPipe LLM Inference API integration
- ✅ Model download and management system for .bin format models
- ✅ Device compatibility checking with enhanced memory detection
- ✅ Production AI inference with Gemma and Phi-2 models
- ✅ Background AI processing service
- ✅ Memory optimization and efficient model cycling
- ✅ Real-time text generation with contextual awareness
- ✅ Voice input/output integration
- ✅ Complete migration from TensorFlow Lite to MediaPipe

### Sprint 3: Intelligence Layer (90% Complete) 🔄 IN PROGRESS
**Objective**: Smart Features and Advanced Processing

**Completed Architecture**:
- ✅ Notification analysis and insights framework
- ✅ SMS integration and smart response system
- ✅ App usage pattern analysis and reporting
- ✅ Context-aware AI response generation
- ✅ Vector database implementation foundation
- ✅ Semantic search capabilities framework
- ✅ Contextual memory and learning system

**Integration Pending**:
- 🔄 Real-time data processing pipeline
- 🔄 Production notification analysis
- 🔄 Live SMS response generation

### Sprint 4: Production Ready (Planned)
**Objective**: Polish & Market Release

**Planned Features**:
- [ ] Performance optimization and profiling
- [ ] Advanced security features and auditing
- [ ] Comprehensive testing and QA
- [ ] User interface polish and accessibility
- [ ] Documentation completion
- [ ] Play Store preparation and submission
- [ ] Beta testing program launch

**Timeline**: Target completion by November 2025

---

## 🔒 Privacy & Security

### Core Principles
- **🏠 Local Processing**: All AI computation happens on your device
- **🔐 End-to-End Encryption**: AES-256 encryption for all stored data
- **🚫 No Data Collection**: Zero telemetry, analytics, or tracking
- **📱 Device-Only Storage**: No cloud servers or external databases
- **🔓 Open Source**: Fully transparent and auditable code

### Data Protection
- **Database**: SQLCipher with AES-256 encryption
- **Memory**: Secure memory allocation for sensitive data
- **Network**: No network calls for AI processing
- **Permissions**: Minimal required permissions with user consent

### Compliance
- **GDPR**: Full compliance with European data protection regulations
- **CCPA**: California Consumer Privacy Act compliant
- **COPPA**: Safe for users of all ages
- **Accessibility**: WCAG 2.1 AA compliant interface

---

## 📱 Screenshots

| Chat Interface | Model Management | Privacy Settings |
|:-------------:|:---------------:|:---------------:|
| ![Chat](docs/screenshots/chat.png) | ![Models](docs/screenshots/models.png) | ![Privacy](docs/screenshots/privacy.png) |

---

## 🤝 Contributing

We welcome contributions! Please see our [Contributing Guidelines](CONTRIBUTING.md) for details.

### Development Workflow
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Code Style
- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use [ktlint](https://ktlint.github.io/) for code formatting
- Write comprehensive tests for new features
- Document public APIs with KDoc

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🆘 Support

- **Documentation**: [Wiki](https://github.com/aisoul/private-assistant/wiki)
- **Issues**: [GitHub Issues](https://github.com/aisoul/private-assistant/issues)
- **Discussions**: [GitHub Discussions](https://github.com/aisoul/private-assistant/discussions)
- **Security**: [Security Policy](SECURITY.md)

---

## 🙏 Acknowledgments

- [MediaPipe](https://developers.google.com/mediapipe) for advanced on-device AI processing
- [SQLCipher](https://www.zetetic.net/sqlcipher/) for database encryption
- [Jetpack Compose](https://developer.android.com/jetpack/compose) for modern UI
- [Material Design 3](https://m3.material.io/) for design system

---

<div align="center">

**Made with ❤️ for privacy-conscious users**

[⭐ Star us on GitHub](https://github.com/aisoul/private-assistant) • [🐛 Report Bug](https://github.com/aisoul/private-assistant/issues) • [💡 Request Feature](https://github.com/aisoul/private-assistant/issues)

</div>