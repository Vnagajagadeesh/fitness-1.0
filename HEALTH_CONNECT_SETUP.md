# FITNESS 1.0 — HEALTH CONNECT SETUP GUIDE
**Automatic Step Tracking for POCO X7 Pro (Xiaomi HyperOS / Android 14+)**

---

## 📱 Background & Integration

Your POCO X7 Pro comes with built-in hardware pedometer sensors and displays step records through Xiaomi system health features. Android Health Connect aggregates step records from:
1. **POCO X7 Pro hardware sensors**
2. **Google Play Services**
3. **Fitness apps**

**FITNESS 1.0** communicates directly with Android Health Connect to read daily steps and calculate your progress toward the **10,000 steps** daily discipline goal (Habit #9).

---

## ⚙️ Setup Instructions on your POCO X7 Pro

### Step 1: Verify Health Connect on your phone
1. On your POCO X7 Pro, open **Settings** → **Security & privacy** → **Health Connect** (or search "Health Connect" in phone Settings).
2. Ensure **Steps** data permission is available.
3. In **Data and access** → **Steps**, you should see **POCO X7 Pro** and **Google Play Services** listed.

### Step 2: Grant Permission in FITNESS 1.0
1. Open **FITNESS 1.0**.
2. Tap on the **Steps** tab or the Steps card on the Dashboard.
3. You will see a banner: `"POCO X7 Pro • Health Connect - Permission needed"`.
4. Tap **Grant Permission**.
5. The Android system Health Connect permission dialog will appear: toggle **Allow FITNESS 1.0 to read Steps** and tap **Allow**.

### Step 3: Automatic Step Sync & Habit #9 Auto-Completion
- Once granted, whenever you open the app or tap the **Sync / Refresh** button, FITNESS 1.0 reads today's total steps.
- When you reach **10,000 steps**, Habit #9 ("10,000 steps") automatically checks off in your daily checklist!

---

## 🛡️ Deduplication & Fallback

- **No Double Counting**: Health Connect handles deduplication across overlapping time slices when multiple services log steps simultaneously.
- **Manual Step Fallback**: If Health Connect is ever disabled or temporarily restricted by HyperOS battery optimization, you can tap **Manual Entry** on the Steps screen to enter your step count directly.

