-- ==============================================================================
-- FITNESS 1.0 — SUPABASE SCHEMA & SECURITY POLICIES
-- Project: Fitness-season-1
-- Table:   season10_progress
-- ==============================================================================

-- 1. Create table for encrypted user progress backups
CREATE TABLE IF NOT EXISTS public.season10_progress (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id TEXT NOT NULL,
    data TEXT NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- 2. Create index on user_id for high-speed queries
CREATE INDEX IF NOT EXISTS idx_season10_progress_user_id ON public.season10_progress(user_id);

-- 3. Enable Row-Level Security (RLS)
ALTER TABLE public.season10_progress ENABLE ROW LEVEL SECURITY;

-- 4. Policy: Allow read operations with public anon key
CREATE POLICY "Allow anon read for user"
ON public.season10_progress
FOR SELECT
TO anon
USING (true);

-- 5. Policy: Allow upsert/insert operations with public anon key
CREATE POLICY "Allow anon upsert for user"
ON public.season10_progress
FOR ALL
TO anon
USING (true)
WITH CHECK (true);

-- 6. Trigger to automatically update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ language 'plpgsql';

DROP TRIGGER IF EXISTS update_season10_progress_updated_at ON public.season10_progress;
CREATE TRIGGER update_season10_progress_updated_at
    BEFORE UPDATE ON public.season10_progress
    FOR EACH ROW
    EXECUTE PROCEDURE update_updated_at_column();

