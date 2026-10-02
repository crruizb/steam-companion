-- Achievement details per game from Steam: names and icons (GetSchemaForGame) and the share of
-- players who unlocked each one (GetGlobalAchievementPercentagesForApp). Shared by all users
CREATE TABLE game_achievements (
    app_id INTEGER NOT NULL,
    api_name VARCHAR NOT NULL,
    display_name VARCHAR NOT NULL,
    description VARCHAR,
    icon_url VARCHAR,
    icon_gray_url VARCHAR,
    hidden BOOLEAN NOT NULL DEFAULT FALSE,
    global_percent DOUBLE PRECISION,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (app_id, api_name)
);
