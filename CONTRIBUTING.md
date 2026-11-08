# Contributing to FITNESS 1.0 — THE BEGINNING

Thank you for your interest in contributing to **FITNESS 1.0**. We welcome contributions that improve code quality, documentation, performance, or accessibility while maintaining the high standards of this native Android application.

---

## 🏗️ Architecture & Guidelines

1. **Language & Framework**: Kotlin 1.9.23, Jetpack Compose with Material 3.
2. **Architecture**: MVVM with Repository pattern, Kotlin Coroutines, and StateFlow.
3. **Database**: Room Database with strict entity separation and type converters.
4. **Health Connect**: Compatible with Health Connect Android SDK (`1.1.0-alpha07`), adhering to granular read permissions for Steps (`READ_STEPS`).
5. **UI / Styling**: Consistent dark holographic aesthetic (`DeepBlackNavy`, `NeonElectricBlue`, `NeonCyan`, `NeonPurple`, `NeonAmber`, `NeonGreen`). All cards use glassmorphism with glowing borders.
6. **Zero External Secrets**: No hardcoded API keys, tokens, or private credentials in version control.

---

## 🛠️ Local Development & Build

1. **Prerequisites**:
   - JDK 17 (LTS)
   - Android SDK (API 34 / Build Tools 34.0.0)
2. **Build Debug APK**:
   ```powershell
   .\gradlew assembleDebug
   ```
   Or use the automated batch script:
   ```cmd
   build-debug-apk.bat
   ```
3. **Run Unit Tests**:
   ```powershell
   .\gradlew testDebugUnitTest
   ```

---

## 📝 Pull Request Workflow

1. Fork the repository and create your feature branch:
   ```bash
   git checkout -b feature/your-feature-name
   ```
2. Commit your changes following conventional commit syntax:
   - `feat: add habit reordering animation`
   - `fix: resolve step count deduplication edge case`
   - `docs: update Health Connect guide for HyperOS`
3. Ensure all unit tests pass:
   ```bash
   .\gradlew testDebugUnitTest
   ```
4. Submit a Pull Request targeting the `main` branch. Provide clear test notes and screenshots/recordings for UI changes.
