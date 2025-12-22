CREATE TABLE achievements (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGSERIAL NOT NULL,
    app_id INTEGER NOT NULL,
    name VARCHAR NOT NULL,
    achieved BOOLEAN DEFAULT FALSE,
    unlock_time TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user
        FOREIGN KEY(user_id)
            REFERENCES users(id)
            ON DELETE CASCADE
);

CREATE TRIGGER update_achievements_updated_at
    BEFORE UPDATE ON achievements
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
