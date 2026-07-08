package com.auracxeli.connections;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ConnectionsSessionRepository extends JpaRepository<ConnectionsSession, Long> {
    Optional<ConnectionsSession> findByPuzzleDateAndUserId(LocalDate puzzleDate, Long userId);

    List<ConnectionsSession> findByUserIdOrderByPuzzleDateAsc(Long userId);
}
