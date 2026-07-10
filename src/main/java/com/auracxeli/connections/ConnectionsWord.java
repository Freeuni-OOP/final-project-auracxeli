package com.auracxeli.connections;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
// this class is for the single word in connections
@Getter
@Entity
@Table(name = "connections_words")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConnectionsWord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private ConnectionsGroup group;
    @Column(name = "word", nullable = false, length = 50)
    private String word;

    public ConnectionsWord(ConnectionsGroup group, String word) {
        this.group = group;
        this.word = word;
    }
}