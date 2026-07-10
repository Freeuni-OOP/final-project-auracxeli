package com.auracxeli.connections;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ConnectionsSessionRepository extends JpaRepository<ConnectionsSession, Long> {
    Optional<ConnectionsSession> findByPuzzleDateAndUserId(LocalDate puzzleDate, Long userId);

    List<ConnectionsSession> findByUserIdOrderByPuzzleDateAsc(Long userId);

    boolean existsByUserIdAndOutcomeAndMistakesCount(Long userId, ConnectionsOutcome outcome, int mistakesCount);

    @Query("""
            SELECT DISTINCT s
            FROM ConnectionsSession s
            LEFT JOIN FETCH s.guesses
            WHERE s.user.id = :userId
            ORDER BY s.puzzleDate DESC
            """)
    List<ConnectionsSession> findByUserIdOrderByPuzzleDateDesc(@Param("userId") Long userId);

     // Count of the user's finished games (anything except IN_PROGRESS)
    long countByUserIdAndOutcomeNot(Long userId, ConnectionsOutcome outcome);
}
