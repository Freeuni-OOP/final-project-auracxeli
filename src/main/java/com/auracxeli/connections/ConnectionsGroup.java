package com.auracxeli.connections;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// here is the group for 4 words that have 4 dificulty levels,they will have each their color but I will do that in frontend part

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
