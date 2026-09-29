-- When each game was last played, from Steam (null if never played or not imported since)
ALTER TABLE user_games ADD COLUMN last_played_at TIMESTAMPTZ;
