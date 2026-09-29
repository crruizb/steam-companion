-- Store a SHA-256 hash of each refresh token instead of the token itself.
-- Existing rows are hashed in place, so current sessions keep working.
ALTER TABLE refresh_tokens RENAME COLUMN token TO token_hash;
UPDATE refresh_tokens SET token_hash = encode(sha256(convert_to(token_hash, 'UTF8')), 'hex');
