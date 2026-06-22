CREATE TABLE wordle_words (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    word           VARCHAR(10) NOT NULL UNIQUE,
    scheduled_date DATE        UNIQUE,
    added_by       BIGINT      NULL,
    created_at     DATETIME    NOT NULL,
    CONSTRAINT fk_wordle_words_added_by
        FOREIGN KEY (added_by) REFERENCES users (id) ON DELETE SET NULL
);
