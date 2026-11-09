@echo off
title FITNESS 1.0 - Build Debug APK
cd /d "%~dp0"
echo ========================================================
echo   FITNESS 1.0 - Building Native Android Debug APK
echo ========================================================
echo.

if exist "%~dp0tools\jdk-17\jdk-17.0.12+7" (
    set "JAVA_HOME=%~dp0tools\jdk-17\jdk-17.0.12+7"
) else (
    set "JAVA_HOME=D:\FITNESS\tools\jdk-17\jdk-17.0.12+7"
)

if exist "%~dp0android-sdk" (
    set "ANDROID_HOME=%~dp0android-sdk"
) else (
    set "ANDROID_HOME=D:\FITNESS\android-sdk"
)
set "Path=%JAVA_HOME%\bin;%ANDROID_HOME%\platform-tools;%Path%"

call gradlew.bat assembleDebug
if %ERRORLEVEL% EQU 0 (
    copy /Y "%~dp0app\build\outputs\apk\debug\app-debug.apk" "%~dp0FITNESS-1.0-debug.apk" >nul
    echo.
    echo ========================================================
    echo   BUILD SUCCESSFUL!
    echo   APK Generated: %~dp0FITNESS-1.0-debug.apk
    echo ========================================================
) else (
    echo.
    echo [ERROR] Build failed. Please review errors above.
)
pause

