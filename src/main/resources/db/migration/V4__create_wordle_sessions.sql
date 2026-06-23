-- first let's create main session table
CREATE TABLE wordle_sessions
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT      NOT NULL,
    puzzle_date DATE        NOT NULL,
    outcome     VARCHAR(15) NOT NULL,
    created_at  TIMESTAMP   NOT NULL,
    CONSTRAINT fk_session_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT chk_outcome CHECK (outcome IN ('IN_PROGRESS', 'WON', 'LOST')),
    CONSTRAINT uk_user_puzzle UNIQUE (user_id, puzzle_date) -- each user can have only one try per day
);

-- I will create the index on user_id because we look at the games by user
CREATE INDEX idx_wordle_sessions_user ON wordle_sessions (user_id);

-- now create guesses table in a session
CREATE TABLE wordle_guesses
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id   BIGINT      NOT NULL,
    guess_word   VARCHAR(10) NOT NULL,
    guess_number TINYINT     NOT NULL,
    created_at   TIMESTAMP   NOT NULL,
    CONSTRAINT fk_guess_session FOREIGN KEY (session_id)
        REFERENCES wordle_sessions (id) ON DELETE CASCADE,
    CONSTRAINT uk_session_guess_number UNIQUE (session_id, guess_number) -- each guess in a session has to be unique
);

CREATE INDEX idx_wordle_guesses_session ON wordle_guesses (session_id);