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


}
