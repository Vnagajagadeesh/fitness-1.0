# FITNESS 1.0 — RELEASE SIGNING & BUILD GUIDE

This guide explains how to generate a signed, production-ready release APK for **FITNESS 1.0**.

---

## 🔑 Step 1: Generate a Release Keystore

Run the following command in PowerShell / Terminal:

```powershell
keytool -genkey -v -keystore fitness-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias fitness -storetype JKS
```

You will be prompted to enter:
- Keystore password (choose a strong password and save it)
- Key password
- Organization information (e.g. `JD / Fitness 1.0`)

Place the generated `fitness-release-key.jks` in a secure location (never commit passwords to public git repositories).

---

## 🛠️ Step 2: Configure Signing in `app/build.gradle.kts`

In `C:\FITNESS\app\build.gradle.kts`, add your signing configuration:

```kotlin
android {
    ...
    signingConfigs {
        create("release") {
            storeFile = file("fitness-release-key.jks")
            storePassword = System.getenv("FITNESS_KEYSTORE_PASSWORD") ?: "your_keystore_password"
            keyAlias = "fitness"
            keyPassword = System.getenv("FITNESS_KEY_PASSWORD") ?: "your_key_password"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

---

## 📦 Step 3: Build the Signed Release APK

Run the Gradle command:

```powershell
.\gradlew assembleRelease
```

The signed release APK will be generated at:
```
app\build\outputs\apk\release\app-release.apk
```

---

## 📲 Step 4: Installing on your POCO X7 Pro

1. Connect your POCO X7 Pro to your computer via USB (select "File Transfer" / MTP mode).
2. Copy `app-debug.apk` or `app-release.apk` into your phone's **Downloads** folder.
3. On your POCO X7 Pro, open **File Manager** → **Downloads** → tap `app-debug.apk`.
4. Tap **Install** (if prompted by Xiaomi HyperOS, allow "Install from unknown sources" for File Manager).
5. Open **FITNESS 1.0** and begin your 90-Day transformation!

