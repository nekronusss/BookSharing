-- Apply once to an existing BookSharing PostgreSQL schema before starting with DDL_AUTO=validate.
-- Repeatable and additive. No pgvector extension or administrator privilege required.
BEGIN;
CREATE TABLE IF NOT EXISTS ai_book_embedding (
    book_id bigint PRIMARY KEY REFERENCES book(id) ON DELETE CASCADE,
    content_hash varchar(64) NOT NULL,
    model_key varchar(64) NOT NULL,
    vector_json text NOT NULL
);
CREATE TABLE IF NOT EXISTS ai_preference (
    user_id bigint PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    genres_json varchar(2000) NOT NULL DEFAULT '[]',
    interests varchar(500) NOT NULL DEFAULT '',
    use_history boolean NOT NULL DEFAULT false
);
COMMIT;
