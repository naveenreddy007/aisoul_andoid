@echo off
echo ================================
echo    Android Development Setup
echo ================================
echo.

echo Checking Java installation...
java -version
if %errorlevel% neq 0 (
    echo ERROR: Java is not installed or not in PATH!
    echo Please install JDK 17 or later.
    echo Download from: https://adoptium.net/
    echo.
    pause
    exit /b 1
)

echo.
echo Checking Android SDK...
if "%ANDROID_HOME%"=="" (
    echo WARNING: ANDROID_HOME is not set!
    echo.
    echo Please install Android Studio from: https://developer.android.com/studio
    echo After installation, set ANDROID_HOME environment variable to your SDK path.
    echo Typical path: C:\Users\%USERNAME%\AppData\Local\Android\Sdk
    echo.
) else (
    echo ANDROID_HOME is set to: %ANDROID_HOME%
)

echo.
echo Setting up Gradle wrapper...
if not exist "gradle\wrapper\gradle-wrapper.jar" (
    echo Downloading Gradle wrapper...
    powershell -Command "Invoke-WebRequest -Uri 'https://github.com/gradle/gradle/raw/v8.6.0/gradle/wrapper/gradle-wrapper.jar' -OutFile 'gradle\wrapper\gradle-wrapper.jar'"
    if %errorlevel% neq 0 (
        echo Failed to download gradle-wrapper.jar
        echo Please download manually from: https://github.com/gradle/gradle/raw/v8.6.0/gradle/wrapper/gradle-wrapper.jar
        echo And place it in: gradle\wrapper\gradle-wrapper.jar
    )
)

echo.
echo ================================
echo Setup complete!
echo ================================
echo.
echo To build and run the app:
echo   1. Start Android Studio
echo   2. Create/Start an AVD (Android Virtual Device)
echo   3. Run: run-app.bat
echo.
echo Or use gradlew commands directly:
echo   gradlew.bat assembleDebug    - Build the app
echo   gradlew.bat installDebug     - Install on device/emulator
echo.
pause