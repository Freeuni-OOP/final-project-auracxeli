-- Records which achievements each user has earned. One row per (user, achievement).
CREATE TABLE user_achievements (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT      NOT NULL,
    achievement_key VARCHAR(40) NOT NULL,
    earned_at       DATETIME    NOT NULL,
    CONSTRAINT fk_user_achievements_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uk_user_achievement UNIQUE (user_id, achievement_key)
);

CREATE INDEX idx_user_achievements_user ON user_achievements (user_id);
