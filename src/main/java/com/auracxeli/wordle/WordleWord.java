package com.auracxeli.wordle;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Setter;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "wordle_words")
@Getter @Setter
public class WordleWord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String word;

    @Column(name = "scheduled_date", unique = true)
    private LocalDate scheduledDate;

    // FK to users.id. Stored as a plain id (we never need the full User here);
    // null for system-seeded words. See the V2 migration for ON DELETE SET NULL.
    @Column(name = "added_by")
    private Long addedBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected WordleWord() {}

    public WordleWord(String word, LocalDate scheduledDate, Long addedBy) {
        this.word = word;
        this.scheduledDate = scheduledDate;
        this.addedBy = addedBy;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }









}
