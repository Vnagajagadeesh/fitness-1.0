@echo off
setlocal
echo ========================================================
echo   FITNESS 1.0 - Signed Release APK Builder
echo ========================================================

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
set "PATH=%JAVA_HOME%\bin;%ANDROID_HOME%\platform-tools;%PATH%"

echo Building Production Release APK...
call "%~dp0gradlew.bat" assembleRelease

if %ERRORLEVEL% equ 0 (
    echo.
    echo ========================================================
    echo   BUILD SUCCESSFUL!
    echo ========================================================
    copy /y "%~dp0app\build\outputs\apk\release\app-release.apk" "%~dp0FITNESS-1.0-release.apk" >nul
    echo Release APK copied to: %~dp0FITNESS-1.0-release.apk
) else (
    echo.
    echo [ERROR] Build failed! Check output above.
)

pause

