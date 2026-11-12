@echo off
title FITNESS 1.2 - Build Signed Release APK
cd /d "%~dp0"
echo ========================================================
echo   FITNESS 1.2 - Building Signed Production Release APK
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

call gradlew.bat assembleRelease
if %ERRORLEVEL% EQU 0 (
    copy /Y "%~dp0app\build\outputs\apk\release\app-release.apk" "%~dp0FITNESS-1.2-release.apk" >nul
    echo.
    echo ========================================================
    echo   BUILD SUCCESSFUL!
    echo   Release APK: %~dp0FITNESS-1.2-release.apk
    echo ========================================================
) else (
    echo.
    echo [ERROR] Build failed. Please review errors above.
)
pause
