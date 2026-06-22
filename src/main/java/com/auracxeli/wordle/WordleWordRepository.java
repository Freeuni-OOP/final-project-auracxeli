package com.auracxeli.wordle;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface WordleWordRepository extends JpaRepository<WordleWord, Long> {

    // Spring Data turns this into: SELECT ... FROM wordle_words WHERE scheduled_date = ?
    Optional<WordleWord> findByScheduledDate(LocalDate scheduledDate);
}
