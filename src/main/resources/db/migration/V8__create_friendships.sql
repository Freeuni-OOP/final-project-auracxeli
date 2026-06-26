CREATE TABLE friendships
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    requester_id BIGINT      NOT NULL,
    addressee_id BIGINT      NOT NULL,
    status       VARCHAR(10) NOT NULL,
    created_at   DATETIME    NOT NULL,
    CONSTRAINT fk_friendship_requester FOREIGN KEY (requester_id)
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_friendship_addressee FOREIGN KEY (addressee_id)
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT chk_friendship_status CHECK (status IN ('PENDING', 'ACCEPTED')),
    CONSTRAINT chk_friendship_not_self CHECK (requester_id <> addressee_id),
    CONSTRAINT uk_friendship_pair UNIQUE (requester_id, addressee_id)
);

CREATE INDEX idx_friendship_addressee ON friendships (addressee_id);
