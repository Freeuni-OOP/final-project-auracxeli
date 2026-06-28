package com.auracxeli.connections;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Entity
@Getter
@Table(name = "connections_puzzle")
public class ConnectionsPuzzle {
    protected ConnectionsPuzzle() {}
    // this is gonna be our primary key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "puzzle_date", nullable = false)
    private LocalDate puzzleDate;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @OneToMany(mappedBy = "puzzle", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ConnectionsGroup> groups = new ArrayList<>();
    public ConnectionsPuzzle(LocalDate puzzleDate) {
        this.puzzleDate = puzzleDate;
    }
    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
