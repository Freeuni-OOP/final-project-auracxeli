package com.auracxeli.wordle;

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

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 *  I mirror each guess of a user per session here, also each guess number is unique
 */
@Entity
@Table(name = "wordle_guesses")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WordleGuess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private WordleSession session;

    @Column(name = "guess_word", nullable = false, length = 10)
    private String guessWord;

    @Column(name = "guess_number", nullable = false)
    private Short guessNumber;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public WordleGuess(WordleSession session, String guessWord, int guessNumber) {
        this.session = session;
        this.guessWord = guessWord;
        this.guessNumber = (short) guessNumber;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
