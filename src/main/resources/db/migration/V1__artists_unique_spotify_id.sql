DO $$
BEGIN
  -- jeśli tabela nie istnieje, to nic nie rób
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.tables
    WHERE table_schema = 'public' AND table_name = 'artists'
  ) THEN
    RETURN;
  END IF;

  -- usuń duplikaty (zostawiamy "najstarszy" wiersz)
  DELETE FROM artists a
  USING artists b
  WHERE a.spotify_id = b.spotify_id
    AND a.ctid > b.ctid;

  -- dodaj constraint unique tylko jeśli jeszcze go nie ma
  IF NOT EXISTS (
    SELECT 1
    FROM pg_constraint
    WHERE conname = 'uq_artists_spotify_id'
  ) THEN
    ALTER TABLE artists
      ADD CONSTRAINT uq_artists_spotify_id UNIQUE (spotify_id);
  END IF;
END $$;
