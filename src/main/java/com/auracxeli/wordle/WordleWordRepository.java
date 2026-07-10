package com.auracxeli.wordle;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WordleWordRepository extends JpaRepository<WordleWord, Long> {

    // Used by WordleDailyService to fetch today's puzzle.
    Optional<WordleWord> findByScheduledDate(LocalDate scheduledDate);

    // Is a given day already taken? (used when finding the first free day)
    boolean existsByScheduledDate(LocalDate scheduledDate);

    // 60-day uniqueness: is this word already scheduled inside the given date window?
    boolean existsByWordAndScheduledDateBetween(String word, LocalDate start, LocalDate end);

    // Words scheduled within a date range (the admin "upcoming days" list), earliest first.
    List<WordleWord> findByScheduledDateBetweenOrderByScheduledDate(LocalDate start, LocalDate end);

    // Archive: strictly-past puzzles (today stays the once-only daily), newest first.
    List<WordleWord> findByScheduledDateLessThanOrderByScheduledDateDesc(LocalDate date);
}
