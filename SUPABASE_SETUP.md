# FITNESS 1.0 — SUPABASE CLOUD SETUP GUIDE

This guide explains how to connect your native Android application to your existing Supabase project.

---

## 🔒 Security Principles

1. **NO SERVICE-ROLE KEYS**: Never place your Supabase service-role secret key inside the Android app or repository.
2. **USE PUBLIC ANON KEY**: Use only your public project URL and `anon` public API key.
3. **ROW-LEVEL SECURITY (RLS)**: Row-Level Security controls user access per session.

---

## 📋 Existing Project Details

- **Project Name**: `Fitness-season-1`
- **Table Name**: `season10_progress`
- **Session Key**: `season10-session`
- **Local Storage Key**: `season10-data-v1`
- **Cloud Config Key**: `season10-cloud-config`

---

## 🛠️ Table Schema for `season10_progress`

If your table is already created, FITNESS 1.0 connects to it automatically via REST.
If creating or verifying in Supabase SQL Editor:

```sql
CREATE TABLE IF NOT EXISTS public.season10_progress (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id TEXT NOT NULL,
    data TEXT NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- Enable Row Level Security (RLS)
ALTER TABLE public.season10_progress ENABLE ROW LEVEL SECURITY;

-- Allow public read/write with anon key (or filter by user_id)
CREATE POLICY "Allow anon read for user"
ON public.season10_progress
FOR SELECT
TO anon
USING (true);

CREATE POLICY "Allow anon upsert for user"
ON public.season10_progress
FOR ALL
TO anon
USING (true)
WITH CHECK (true);
```

---

## 📱 How to Configure in the App

1. Open **FITNESS 1.0** on your POCO X7 Pro.
2. Navigate to the **Settings** tab.
3. Scroll down to **SUPABASE CLOUD SYNC**.
4. Enter:
   - **Supabase URL**: `https://<your-project-ref>.supabase.co`
   - **Supabase Anon Key**: `<your-public-anon-key>`
5. Tap **Save Cloud Credentials**.
6. Tap **Cloud Backup** to upload your current progress.
7. Tap **Cloud Restore** anytime you switch phones or want to pull cloud logs.

