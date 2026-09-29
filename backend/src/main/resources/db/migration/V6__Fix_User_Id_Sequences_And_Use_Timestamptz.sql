-- user_games.user_id and achievements.user_id were declared BIGSERIAL, which gave each
-- foreign key its own sequence and a nextval() default. They only reference users(id).
ALTER TABLE user_games ALTER COLUMN user_id DROP DEFAULT;
DROP SEQUENCE IF EXISTS user_games_user_id_seq;
ALTER TABLE achievements ALTER COLUMN user_id DROP DEFAULT;
DROP SEQUENCE IF EXISTS achievements_user_id_seq;

-- Store timestamps with a time zone. Existing values were written in UTC.
ALTER TABLE users
    ALTER COLUMN created_at TYPE TIMESTAMPTZ USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE TIMESTAMPTZ USING updated_at AT TIME ZONE 'UTC';

ALTER TABLE user_games
    ALTER COLUMN created_at TYPE TIMESTAMPTZ USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE TIMESTAMPTZ USING updated_at AT TIME ZONE 'UTC';

ALTER TABLE achievements
    ALTER COLUMN unlock_time TYPE TIMESTAMPTZ USING unlock_time AT TIME ZONE 'UTC',
    ALTER COLUMN created_at TYPE TIMESTAMPTZ USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE TIMESTAMPTZ USING updated_at AT TIME ZONE 'UTC';

ALTER TABLE refresh_tokens
    ALTER COLUMN created_at TYPE TIMESTAMPTZ USING created_at AT TIME ZONE 'UTC';
