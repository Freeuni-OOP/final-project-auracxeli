package com.auracxeli.connections;

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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// here are one users attempt in one day
@Getter
@Entity
@Table(name = "connections_sessions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConnectionsSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "puzzle_date", nullable = false)
    private LocalDate puzzleDate;
    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "outcome", nullable = false, length = 15)
    private ConnectionsOutcome outcome;
    @Column(name = "mistakes_count", nullable = false)
    private int mistakesCount;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ConnectionsGuess> guesses = new ArrayList<>();

    public ConnectionsSession(User user, LocalDate puzzleDate) {
        this.user = user;
        this.puzzleDate = puzzleDate;
        this.outcome = ConnectionsOutcome.IN_PROGRESS;
        this.mistakesCount = 0;
    }
    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
    public void incrementMistakes() {
        this.mistakesCount++;
    }
}