package com.auracxeli.wordle;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WordleSessionRepository extends JpaRepository<WordleSession, Long> {

    Optional<WordleSession> findByUserIdAndPuzzleDate(Long userId, LocalDate puzzleDate);

    List<WordleSession> findByUserIdOrderByPuzzleDateAsc(Long userId);
}