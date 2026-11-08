@echo off
setlocal
echo ========================================================
echo   FITNESS 1.0 - Signed Release APK Builder
echo ========================================================

set "JAVA_HOME=%~dp0tools\jdk-17\jdk-17.0.12+7"
set "PATH=%JAVA_HOME%\bin;%PATH%"

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

