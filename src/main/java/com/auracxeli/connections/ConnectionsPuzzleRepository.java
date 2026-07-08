package com.auracxeli.connections;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

public interface ConnectionsPuzzleRepository extends JpaRepository<ConnectionsPuzzle, Long> {

    Optional<ConnectionsPuzzle> findByPuzzleDate(LocalDate puzzleDate);

    boolean existsByPuzzleDate(LocalDate puzzleDate);

    List<ConnectionsPuzzle> findByPuzzleDateGreaterThanEqualOrderByPuzzleDate(LocalDate puzzleDate);
}