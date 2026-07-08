package com.auracxeli.wordle;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WordleSessionRepository extends JpaRepository<WordleSession, Long> {

    Optional<WordleSession> findByUserIdAndPuzzleDate(Long userId, LocalDate puzzleDate);

    List<WordleSession> findByUserIdOrderByPuzzleDateAsc(Long userId);

    @Query("""
            SELECT DISTINCT s
            FROM WordleSession s
            LEFT JOIN FETCH s.guesses
            WHERE s.user.id = :userId
            ORDER BY s.puzzleDate DESC
            """)
    List<WordleSession> findByUserIdOrderByPuzzleDateDesc(@Param("userId") Long userId);

    /**
     * Guess distribution for a user's won games: how many games were won at each
     * guess count. Returns one row per non-empty bucket as
     * {@code [guessCount, gamesWon]} (both BIGINT); buckets with zero wins are absent.
     */
    @Query(value = """
            SELECT sub.num_guesses AS guessCount, COUNT(*) AS gamesWon
            FROM (
                SELECT g.session_id, COUNT(*) AS num_guesses
                FROM wordle_guesses g
                JOIN wordle_sessions s ON s.id = g.session_id
                WHERE s.user_id = :userId AND s.outcome = 'WON'
                GROUP BY g.session_id
            ) sub
            GROUP BY sub.num_guesses
            """, nativeQuery = true)
    List<Object[]> findGuessDistribution(@Param("userId") Long userId);
}
