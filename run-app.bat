@echo off
echo ================================
echo    HelloWorld Kotlin Android App
echo ================================
echo.

echo Checking for Android SDK...
if "%ANDROID_HOME%"=="" (
    echo WARNING: ANDROID_HOME is not set!
    echo Please install Android Studio and set ANDROID_HOME environment variable.
    echo.
)

echo Building the app...
call gradlew.bat clean
if %errorlevel% neq 0 goto error

call gradlew.bat assembleDebug
if %errorlevel% neq 0 goto error

echo.
echo ================================
echo Build successful! 
echo ================================
echo.
echo To install on device/emulator, run:
echo   gradlew.bat installDebug
echo.
echo To start an emulator, run:
echo   emulator -avd YOUR_AVD_NAME
echo.
echo APK location: app\build\outputs\apk\debug\app-debug.apk
echo.
pause
exit /b 0

:error
echo.
echo ================================
echo Build failed! Check the error messages above.
echo ================================
pause
exit /b 1