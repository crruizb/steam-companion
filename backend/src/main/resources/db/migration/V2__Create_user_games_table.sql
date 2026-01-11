CREATE TABLE user_games (
    user_id BIGSERIAL NOT NULL,
    app_id INTEGER NOT NULL,
    name VARCHAR NOT NULL,
    play_time_forever_minutes INTEGER DEFAULT 0,
    img_url VARCHAR,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, app_id),
    CONSTRAINT fk_user
        FOREIGN KEY(user_id)
            REFERENCES users(id)
            ON DELETE CASCADE
);
