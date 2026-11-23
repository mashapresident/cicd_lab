CREATE TABLE users (
    id            UUID PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    display_name  VARCHAR(100) NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL
);

CREATE TABLE refresh_tokens (
    id          UUID PRIMARY KEY,
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash  VARCHAR(64) NOT NULL UNIQUE,
    expires_at  TIMESTAMPTZ NOT NULL
);

CREATE TABLE categories (
    id   SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE podcasts (
    id          UUID PRIMARY KEY,
    title       VARCHAR(500) NOT NULL,
    author      VARCHAR(300) NOT NULL DEFAULT '',
    description TEXT,
    cover_url   TEXT,
    feed_url    VARCHAR(1000) NOT NULL UNIQUE,
    language    VARCHAR(20),
    category_id INT REFERENCES categories(id) ON DELETE SET NULL,
    updated_at  TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_podcasts_title ON podcasts (lower(title));
CREATE INDEX idx_podcasts_author ON podcasts (lower(author));

CREATE TABLE episodes (
    id           UUID PRIMARY KEY,
    podcast_id   UUID NOT NULL REFERENCES podcasts(id) ON DELETE CASCADE,
    title        VARCHAR(500) NOT NULL,
    description  TEXT,
    audio_url    TEXT NOT NULL,
    duration_sec INT,
    published_at TIMESTAMPTZ NOT NULL,
    guid         VARCHAR(500) NOT NULL,
    UNIQUE (podcast_id, guid)
);
CREATE INDEX idx_episodes_podcast_published ON episodes (podcast_id, published_at DESC);

CREATE TABLE subscriptions (
    user_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    podcast_id UUID NOT NULL REFERENCES podcasts(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (user_id, podcast_id)
);

CREATE TABLE favorites (
    user_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    episode_id UUID NOT NULL REFERENCES episodes(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (user_id, episode_id)
);

CREATE TABLE listening_progress (
    user_id      UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    episode_id   UUID NOT NULL REFERENCES episodes(id) ON DELETE CASCADE,
    position_sec INT NOT NULL,
    completed    BOOLEAN NOT NULL DEFAULT FALSE,
    updated_at   TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (user_id, episode_id)
);
