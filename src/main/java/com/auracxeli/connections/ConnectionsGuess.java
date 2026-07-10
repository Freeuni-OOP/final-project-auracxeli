package com.auracxeli.connections;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "connections_guesses")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConnectionsGuess {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ConnectionsSession session;

    @Column(name = "word1", nullable = false, length = 50)
    private String word1;
    @Column(name = "word2", nullable = false, length = 50)
    private String word2;
    @Column(name = "word3", nullable = false, length = 50)
    private String word3;
    @Column(name = "word4", nullable = false, length = 50)
    private String word4;

    @Column(name = "correct", nullable = false)
    private boolean correct;
    @Column(name = "guess_number", nullable = false)
    private Short guessNumber;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public ConnectionsGuess(ConnectionsSession session, String word1, String word2, String word3, String word4,
                            boolean correct, int guessNumber) {
        this.session = session;
        this.word1 = word1;
        this.word2 = word2;
        this.word3 = word3;
        this.word4 = word4;
        this.correct = correct;
        this.guessNumber = (short) guessNumber;
    }
    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
