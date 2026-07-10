package com.auracxeli.connections;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// A group of 4 words at one difficulty level; the per-difficulty color is applied in the frontend.
@Getter
@Entity
@Table(name = "connections_groups")
public class ConnectionsGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "puzzle_id", nullable = false)
    private ConnectionsPuzzle puzzle;

    @Column(name = "category", nullable = false, length = 100)
    private String category;
    @Column(name = "difficulty", nullable = false)
    private Short difficulty;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ConnectionsWord> words = new ArrayList<>();

    protected ConnectionsGroup() {}

    // I wanted to use here the requiredargsconstructor but we dont have final arguments
    public ConnectionsGroup(ConnectionsPuzzle puzzle, String category, int difficulty) {
        this.puzzle = puzzle;
        this.category = category;
        this.difficulty = (short) difficulty;
    }


    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }


}
