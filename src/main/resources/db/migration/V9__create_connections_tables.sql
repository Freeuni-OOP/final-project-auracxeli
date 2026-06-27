-- i use this table'ss each row to define the every day connections puzzle
CREATE TABLE connections_puzzles
(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    puzzle_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT  uk_connections_puzzle_date UNIQUE (puzzle_date)
);
CREATE INDEX idx_connections_puzzles_date ON connections_puzzles (puzzle_date);
-- this table is for the 4 categories that are in the cconnections game
CREATE TABLE connections_groups
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    puzzle_id  BIGINT       NOT NULL,
    category   VARCHAR(100) NOT NULL,
    difficulty TINYINT      NOT NULL,
    created_at TIMESTAMP    NOT NULL,
    CONSTRAINT fk_group_puzzle FOREIGN KEY (puzzle_id)
        REFERENCES connections_puzzles (id) ON DELETE CASCADE,
    CONSTRAINT chk_group_difficulty CHECK (difficulty BETWEEN 1 AND 4)
);
CREATE INDEX idx_connections_groups_puzzle ON connections_groups (puzzle_id);
-- i created this table for the 4 words that are in each category
CREATE TABLE connections_words
(
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id BIGINT      NOT NULL,
    word     VARCHAR(50) NOT NULL,
    CONSTRAINT fk_word_group FOREIGN KEY (group_id)
        REFERENCES connections_groups (id) ON DELETE CASCADE
);
CREATE INDEX idx_connections_words_group ON connections_words (group_id);

-- this exact table is for the user's attempt in a single days connections puzzle
CREATE TABLE connections_sessions
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id        BIGINT      NOT NULL,
    puzzle_date    DATE        NOT NULL,
    outcome        VARCHAR(15) NOT NULL,
    mistakes_count INT         NOT NULL DEFAULT 0,
    created_at     TIMESTAMP   NOT NULL,
    CONSTRAINT fk_connections_session_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT chk_connections_outcome CHECK (outcome IN ('IN_PROGRESS', 'WON', 'LOST')),
    CONSTRAINT uk_connections_user_puzzle UNIQUE (user_id, puzzle_date)
);
CREATE INDEX idx_connections_sessions_user ON connections_sessions (user_id);
CREATE INDEX idx_connections_sessions_puzzle_date ON connections_sessions (puzzle_date);
-- this table is for the each guess
CREATE TABLE connections_guesses
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id   BIGINT      NOT NULL,
    word1        VARCHAR(50) NOT NULL,
    word2        VARCHAR(50) NOT NULL,
    word3        VARCHAR(50) NOT NULL,
    word4        VARCHAR(50) NOT NULL,
    correct      BOOLEAN     NOT NULL,
    guess_number TINYINT     NOT NULL,
    created_at   TIMESTAMP   NOT NULL,
    CONSTRAINT fk_connections_guess_session FOREIGN KEY (session_id)
        REFERENCES connections_sessions (id) ON DELETE CASCADE,
    CONSTRAINT uk_connections_session_guess_number UNIQUE (session_id, guess_number)
);

CREATE INDEX idx_connections_guesses_session ON connections_guesses (session_id);