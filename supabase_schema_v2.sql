-- ==============================================================================
-- FITNESS 1.2 — JD EDITION: SUPABASE CLOUD SCHEMA & RLS POLICIES (V2)
-- Project: Fitness-season-1
-- Compatibility: Native Android REST API (OkHttp / Supabase PostgREST)
-- ==============================================================================

-- ------------------------------------------------------------------------------
-- 1. PRIMARY SNAPSHOT TABLE (Used by FITNESS 1.2 In-App Cloud Sync)
-- Table: season10_progress
-- ------------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS public.season10_progress (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id TEXT NOT NULL DEFAULT 'JD',
    data TEXT NOT NULL, -- Serialized JSON containing all 17 tables (SyncPayload v2)
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
DROP POLICY IF EXISTS "Allow authenticated full access" ON public.season10_progress;
DROP POLICY IF EXISTS "fitness_v2_anon_select" ON public.season10_progress;
DROP POLICY IF EXISTS "fitness_v2_anon_insert" ON public.season10_progress;
DROP POLICY IF EXISTS "fitness_v2_anon_update" ON public.season10_progress;

-- Allow anon public read access (scoped by anon key)
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

-- Optional: Auto-update updated_at timestamp on row modification
CREATE OR REPLACE FUNCTION public.handle_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = timezone('utc'::text, now());
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS set_season10_progress_updated_at ON public.season10_progress;
CREATE TRIGGER set_season10_progress_updated_at
BEFORE UPDATE ON public.season10_progress
FOR EACH ROW
EXECUTE FUNCTION public.handle_updated_at();


-- ==============================================================================
-- 2. RELATIONAL TABLES FOR FITNESS 1.2 (DIRECT MULTI-TABLE CLOUD STORAGE)
-- Use these tables if you want direct relational querying in Supabase Studio
-- ==============================================================================

-- 2.1 RECOVERY CENTRE
CREATE TABLE IF NOT EXISTS public.fitness_recovery_entries (
    date TEXT PRIMARY KEY, -- YYYY-MM-DD
    user_id TEXT NOT NULL DEFAULT 'JD',
    is_rest_day BOOLEAN DEFAULT false,
    sleep_hours NUMERIC(4, 2) DEFAULT 7.0,
    energy_level INT CHECK (energy_level BETWEEN 1 AND 5),
    perceived_recovery TEXT,
    notes TEXT,
    timestamp BIGINT NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT timezone('utc'::text, now()) NOT NULL
);

ALTER TABLE public.fitness_recovery_entries ENABLE ROW LEVEL SECURITY;
CREATE POLICY "recovery_anon_all" ON public.fitness_recovery_entries FOR ALL TO anon, authenticated USING (true) WITH CHECK (true);

-- 2.2 MY DAILY PLANNER
CREATE TABLE IF NOT EXISTS public.fitness_planner_tasks (
    id BIGINT PRIMARY KEY,
    user_id TEXT NOT NULL DEFAULT 'JD',
    date TEXT NOT NULL, -- YYYY-MM-DD
    title TEXT NOT NULL,
    category TEXT NOT NULL, -- Morning Routine, College, Study, Fitness, Skincare, Meals, Tasks, Evening, Sleep
    start_time TEXT NOT NULL, -- HH:mm
    end_time TEXT NOT NULL, -- HH:mm
    is_completed BOOLEAN DEFAULT false,
    order_index INT DEFAULT 0,
    created_at BIGINT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_planner_date ON public.fitness_planner_tasks (user_id, date, start_time);
ALTER TABLE public.fitness_planner_tasks ENABLE ROW LEVEL SECURITY;
CREATE POLICY "planner_anon_all" ON public.fitness_planner_tasks FOR ALL TO anon, authenticated USING (true) WITH CHECK (true);

-- 2.3 DISCIPLINE MILESTONES & ACHIEVEMENTS
CREATE TABLE IF NOT EXISTS public.fitness_achievements (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL DEFAULT 'JD',
    title TEXT NOT NULL,
    description TEXT NOT NULL,
    category TEXT NOT NULL, -- HABITS, FITNESS, PRODUCTIVITY
    icon_name TEXT,
    target_value INT NOT NULL,
    current_progress INT DEFAULT 0,
    is_unlocked BOOLEAN DEFAULT false,
    unlocked_at BIGINT
);

ALTER TABLE public.fitness_achievements ENABLE ROW LEVEL SECURITY;
CREATE POLICY "achievements_anon_all" ON public.fitness_achievements FOR ALL TO anon, authenticated USING (true) WITH CHECK (true);

-- 2.4 WEEKLY INTELLIGENCE REPORTS
CREATE TABLE IF NOT EXISTS public.fitness_weekly_reports (
    id BIGINT PRIMARY KEY,
    user_id TEXT NOT NULL DEFAULT 'JD',
    week_start_date TEXT NOT NULL, -- YYYY-MM-DD
    week_end_date TEXT NOT NULL, -- YYYY-MM-DD
    habit_compliance_rate NUMERIC(5, 2) DEFAULT 0.0,
    best_habit_title TEXT,
    worst_habit_title TEXT,
    average_daily_steps INT DEFAULT 0,
    total_workouts INT DEFAULT 0,
    skincare_rate NUMERIC(5, 2) DEFAULT 0.0,
    rest_days_count INT DEFAULT 0,
    suggestions_json TEXT, -- JSON array of 3 actionable suggestions
    created_at BIGINT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_reports_week ON public.fitness_weekly_reports (user_id, week_start_date DESC);
ALTER TABLE public.fitness_weekly_reports ENABLE ROW LEVEL SECURITY;
CREATE POLICY "reports_anon_all" ON public.fitness_weekly_reports FOR ALL TO anon, authenticated USING (true) WITH CHECK (true);

-- 2.5 MY PRIVATE JOURNAL (Encrypted / Local-First)
CREATE TABLE IF NOT EXISTS public.fitness_journal_entries (
    id BIGINT PRIMARY KEY,
    user_id TEXT NOT NULL DEFAULT 'JD',
    date TEXT NOT NULL, -- YYYY-MM-DD
    title TEXT,
    content TEXT NOT NULL,
    mood TEXT, -- Great, Good, Neutral, Challenging, Exhausted
    accomplishments TEXT,
    challenges TEXT,
    lessons_learned TEXT,
    tomorrow_goals TEXT,
    allow_ai_insights BOOLEAN DEFAULT false,
    timestamp BIGINT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_journal_date ON public.fitness_journal_entries (user_id, date DESC);
ALTER TABLE public.fitness_journal_entries ENABLE ROW LEVEL SECURITY;
CREATE POLICY "journal_anon_all" ON public.fitness_journal_entries FOR ALL TO anon, authenticated USING (true) WITH CHECK (true);

-- 2.6 FASTING 2.0 SCHEDULES
CREATE TABLE IF NOT EXISTS public.fitness_fasting_schedules (
    id BIGINT PRIMARY KEY,
    user_id TEXT NOT NULL DEFAULT 'JD',
    name TEXT NOT NULL,
    fasting_hours INT NOT NULL,
    eating_hours INT NOT NULL,
    description TEXT,
    is_custom BOOLEAN DEFAULT false,
    created_at BIGINT NOT NULL
);

ALTER TABLE public.fitness_fasting_schedules ENABLE ROW LEVEL SECURITY;
CREATE POLICY "fasting_schedules_anon_all" ON public.fitness_fasting_schedules FOR ALL TO anon, authenticated USING (true) WITH CHECK (true);

-- 2.7 MOTIVATION & PRINCIPLES
CREATE TABLE IF NOT EXISTS public.fitness_motivation_quotes (
    id BIGINT PRIMARY KEY,
    user_id TEXT NOT NULL DEFAULT 'JD',
    quote TEXT NOT NULL,
    author TEXT DEFAULT 'JD Principles',
    category TEXT NOT NULL,
    is_favorite BOOLEAN DEFAULT false,
    saved_at BIGINT NOT NULL
);

ALTER TABLE public.fitness_motivation_quotes ENABLE ROW LEVEL SECURITY;
CREATE POLICY "motivation_anon_all" ON public.fitness_motivation_quotes FOR ALL TO anon, authenticated USING (true) WITH CHECK (true);

-- 2.8 FITNESS AI COACH INSIGHTS
CREATE TABLE IF NOT EXISTS public.fitness_coach_insights (
    id BIGINT PRIMARY KEY,
    user_id TEXT NOT NULL DEFAULT 'JD',
    date TEXT NOT NULL, -- YYYY-MM-DD
    personality_state TEXT NOT NULL, -- HIGH_PERFORMANCE, STRUGGLING, INCONSISTENT, RECOVERY_NEEDED
    guidance TEXT NOT NULL,
    evidence_summary TEXT NOT NULL,
    is_rule_based BOOLEAN DEFAULT true,
    created_at BIGINT NOT NULL
);

ALTER TABLE public.fitness_coach_insights ENABLE ROW LEVEL SECURITY;
CREATE POLICY "coach_anon_all" ON public.fitness_coach_insights FOR ALL TO anon, authenticated USING (true) WITH CHECK (true);

-- ==============================================================================
-- 3. VERIFICATION QUERY
-- Run this in Supabase SQL Editor to confirm table creation:
-- ==============================================================================
-- SELECT table_name, rowsecurity FROM information_schema.tables WHERE table_schema = 'public';

