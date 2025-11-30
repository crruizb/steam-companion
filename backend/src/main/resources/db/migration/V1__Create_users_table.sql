CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    steam_id VARCHAR(20) NOT NULL UNIQUE,
    username VARCHAR(100) NOT NULL,
    display_name VARCHAR(100),
    avatar_url TEXT,
    profile_url TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_steam_id ON users(steam_id);

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
