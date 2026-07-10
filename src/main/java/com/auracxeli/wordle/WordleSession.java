package com.auracxeli.wordle;

import com.auracxeli.user.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

/**
 * I store info about one users one attempt that is limited to 1 per day, it is unique in date
 */
@Entity
@Table(name = "wordle_sessions")
@Getter
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

    public void setOutcome(WordleOutcome outcome) {
        this.outcome = outcome;
    }
}
