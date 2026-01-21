DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.tables
    WHERE table_schema = 'public' AND table_name = 'tracks'
  ) THEN
    RETURN;
  END IF;

  DELETE FROM tracks a
  USING tracks b
  WHERE a.spotify_id = b.spotify_id
    AND a.ctid > b.ctid;

  IF NOT EXISTS (
    SELECT 1
    FROM pg_constraint
    WHERE conname = 'uq_tracks_spotify_id'
  ) THEN
    ALTER TABLE tracks
      ADD CONSTRAINT uq_tracks_spotify_id UNIQUE (spotify_id);
  END IF;
END $$;
