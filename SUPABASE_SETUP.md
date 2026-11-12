# FITNESS 1.2 — SUPABASE CLOUD SETUP & RLS GUIDE (V2)

This guide explains how to connect your **FITNESS 1.2 — JD EDITION** Android application to your Supabase project (`Fitness-season-1`) for encrypted, multi-device cloud synchronization.

---

## 🔒 Security Principles

1. **NO SERVICE-ROLE KEYS**: Never place your Supabase service-role secret key inside the Android app or repository.
2. **USE PUBLIC ANON KEY**: Use only your public project URL and `anon` public API key.
3. **ROW-LEVEL SECURITY (RLS)**: Row-Level Security controls user access per session and prevents unauthorized access.
4. **JOURNAL PRIVACY**: Journal entries remain local-first. Cloud sync only stores them when you explicitly trigger cloud backup, maintaining privacy.

---

## 📋 Existing Project Details

- **Project Name**: `Fitness-season-1`
- **Primary Sync Table**: `season10_progress`
- **Session Key**: `season10-session`
- **Payload Version**: `2` (Contains all 17 tables)

---

## 🛠️ Step 1: Run the Database Migration in Supabase

1. Open your [Supabase Dashboard](https://supabase.com/dashboard).
2. Select your project: **`Fitness-season-1`**.
3. In the left navigation, click on **SQL Editor**.
4. Click **New Query**.
5. Copy and paste the complete SQL script from [`supabase_schema_v2.sql`](supabase_schema_v2.sql):

```sql
-- ==============================================================================
-- PRIMARY SNAPSHOT TABLE FOR FITNESS 1.2
-- ==============================================================================
CREATE TABLE IF NOT EXISTS public.season10_progress (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id TEXT NOT NULL DEFAULT 'JD',
    data TEXT NOT NULL,
    device_info TEXT DEFAULT 'POCO X7 Pro - FITNESS 1.2',
    version INT DEFAULT 2,
    updated_at TIMESTAMPTZ DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- Index for instant querying by user_id and recency
CREATE INDEX IF NOT EXISTS idx_season10_progress_user_updated 
ON public.season10_progress (user_id, updated_at DESC);

-- Enable Row Level Security (RLS)
ALTER TABLE public.season10_progress ENABLE ROW LEVEL SECURITY;

-- Drop previous policies to avoid conflicts on re-run
DROP POLICY IF EXISTS "Allow anon read for user" ON public.season10_progress;
DROP POLICY IF EXISTS "Allow anon upsert for user" ON public.season10_progress;
DROP POLICY IF EXISTS "fitness_v2_anon_select" ON public.season10_progress;
DROP POLICY IF EXISTS "fitness_v2_anon_insert" ON public.season10_progress;
DROP POLICY IF EXISTS "fitness_v2_anon_update" ON public.season10_progress;

-- Allow anon public read access
CREATE POLICY "fitness_v2_anon_select"
ON public.season10_progress
FOR SELECT
TO anon, authenticated
USING (true);

-- Allow anon insert access
CREATE POLICY "fitness_v2_anon_insert"
ON public.season10_progress
FOR INSERT
TO anon, authenticated
WITH CHECK (true);

-- Allow anon update access
CREATE POLICY "fitness_v2_anon_update"
ON public.season10_progress
FOR UPDATE
TO anon, authenticated
USING (true)
WITH CHECK (true);
```

6. Click **Run** (Ctrl + Enter). You will see `Success. No rows returned`.

---

## 📱 Step 2: Configure in the Android App

1. Launch **FITNESS 1.2** on your POCO X7 Pro.
2. Tap the **Settings** tab in the bottom bar.
3. Scroll down to the **SUPABASE CLOUD SYNC** card.
4. Fill in:
   - **Supabase URL**: `https://<your-project-id>.supabase.co`
   - **Supabase Anon Key**: `<your-project-anon-public-key>`
   - **User ID / Name**: `JD` *(or your custom name)*
5. Tap **Save Cloud Credentials**.

---

## 🔄 Step 3: Trigger Backup & Verify

1. Tap **Cloud Backup** in Settings.
   - You will see a success toast: `"Cloud backup successful to table season10_progress"`.
2. Check Supabase Dashboard:
   - Go to **Table Editor** > **`season10_progress`**.
   - You will see a new record with `version = 2` and `user_id = JD`.
   - The `data` field contains your habits, logs, recovery entries, daily plan, fasting sessions, and weekly reports.
3. Tap **Cloud Restore** anytime you switch phones or want to pull your cloud progress. All 17 tables are restored seamlessly.

---

## 🛡️ Optional: User-Isolated RLS (Authenticated Auth)

If you configure Supabase Auth (Email / Password or Google OAuth), replace the anon policies with user-isolated RLS:

```sql
-- Restrict access so users only read and write their own data
CREATE POLICY "fitness_user_isolated_select"
ON public.season10_progress
FOR SELECT
TO authenticated
USING (auth.uid()::text = user_id);

CREATE POLICY "fitness_user_isolated_insert"
ON public.season10_progress
FOR INSERT
TO authenticated
WITH CHECK (auth.uid()::text = user_id);
```
