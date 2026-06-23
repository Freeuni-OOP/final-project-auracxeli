package com.auracxeli.wordle;

import com.auracxeli.user.User;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * I store info about one users one attempt that is limited to 1 per day, it is unique in date
 */
@Entity
@Table(name = "wordle_sessions")
public class WordleSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "puzzle_date", nullable = false)
    private LocalDate puzzleDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "outcome", nullable = false, length = 15)
    private WordleOutcome outcome;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WordleGuess> guesses = new ArrayList<>();

    protected WordleSession() {}

    public WordleSession(User user, LocalDate puzzleDate) {
        this.user = user;
        this.puzzleDate = puzzleDate;
        this.outcome = WordleOutcome.IN_PROGRESS;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId()                  { return id; }
    public User getUser()                { return user; }
    public LocalDate getPuzzleDate()      { return puzzleDate; }
    public WordleOutcome getOutcome()     { return outcome; }
    public LocalDateTime getCreatedAt()   { return createdAt; }
    public List<WordleGuess> getGuesses() { return guesses; }

    public void setOutcome(WordleOutcome outcome) { this.outcome = outcome; }
}