package com.auracxeli.wordle;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import lombok.Getter;



/**
 *  I mirror each guess of a user per session here, also each guess number is unique
 */
@Entity
@Table(name = "wordle_guesses")
@Getter
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

    protected WordleGuess() {}

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
/*


    public Long getId()                 { return id; }
    public WordleSession getSession()   { return session; }
    public String getGuessWord()        { return guessWord; }
    public Short getGuessNumber()       { return guessNumber; }
    public LocalDateTime getCreatedAt() { return createdAt; }

 */
}