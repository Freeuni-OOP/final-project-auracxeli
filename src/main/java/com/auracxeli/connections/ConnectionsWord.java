package com.auracxeli.connections;

import jakarta.persistence.*;
import lombok.Getter;
// this class is for the single word in connections
@Getter
@Entity
@Table(name = "connections_words")
public class ConnectionsWord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private ConnectionsGroup group;
    @Column(name = "word", nullable = false, length = 50)
    private String word;

    protected ConnectionsWord() {}

    public ConnectionsWord(ConnectionsGroup group, String word) {
        this.group = group;
        this.word = word;
    }
}