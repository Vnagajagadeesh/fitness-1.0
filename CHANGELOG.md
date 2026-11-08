# Changelog — FITNESS 1.0 — THE BEGINNING

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.0.0] - 2026-11-08

### Added
- **Core Visual Identity**: Holographic Command Center theme in dark navy with neon blue, cyan, purple, amber, and green accents. Glassmorphism cards with subtle glowing borders and custom JD logo badge.
- **The 90-Day Challenge**: Configurable start/end dates (default 3 Nov 2026 – 31 Jan 2027), daily progress percentage, milestone trackers (Day 30 Foundation, Day 60 Momentum, Day 90 Transformation).
- **Daily Habit Tracker**: Pre-configured with the 13 original discipline habits across 6 categories (Personal Care, Study, Fitness, Nutrition, Mindset, Custom). Fully editable, dynamic streaks, and automatic date rollover.
- **Body Weight Tracker**: Multi-entry per day logging in kilograms with timestamp notes, interactive Compose line charts, and auto-calculated daily, weekly, monthly, and all-time averages.
- **Skincare Routine Tracker**: Independent morning (Face wash, Moisturizer, Sunscreen) and night (Face wash, Moisturizer, Optional treatment) routines with step checklist.
- **Intermittent Fasting Module**: Real-time fasting timer supporting 16:8, 18:6, 20:4, and Custom protocols, progress ring, stage breakdown (Anabolic, Catabolic, Fat Burning, Ketosis, Autophagy), and eating window meal logging.
- **Step Tracking & Health Connect**: Native Android Health Connect SDK integration for deduplicated daily step count reading, 10,000-step auto-check off, and manual fallback logging.
- **Interactive Calendar**: Full-month view displaying habit completion dots, date selection, historical record inspection, and editing past days without data loss.
- **Analytics & Trends**: Multi-habit consistency heatmaps, 7-day completion bar charts, and overall discipline score calculation.
- **Cloud Sync & Legacy Migration**: Supabase REST integration for `Fitness-season-1` project (`season10_progress` table) with JSON backup/restore and safe legacy migration preserving historical skincare records.
- **Local Toolchain & Build Automation**: Zero-admin OpenJDK 17 + Android SDK 34 portable setup with `build-debug-apk.bat` for one-click APK generation.
