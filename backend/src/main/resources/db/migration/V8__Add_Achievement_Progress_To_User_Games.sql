-- Per-game achievement progress, from Steam during the achievements import.
-- Null until the game has been through an import; total is 0 for games without achievements
ALTER TABLE user_games ADD COLUMN achievements_total INTEGER;
ALTER TABLE user_games ADD COLUMN achievements_unlocked INTEGER;
