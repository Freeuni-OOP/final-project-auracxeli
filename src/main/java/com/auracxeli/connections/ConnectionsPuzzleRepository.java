package com.auracxeli.connections;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface ConnectionsPuzzleRepository extends JpaRepository<ConnectionsPuzzle, Long> {

    Optional<ConnectionsPuzzle> findByPuzzleDate(LocalDate puzzleDate);
}