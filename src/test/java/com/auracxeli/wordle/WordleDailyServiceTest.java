package com.auracxeli.wordle;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WordleDailyServiceTest {

    @Mock
    private WordleWordRepository wordleWordRepository;

    @InjectMocks
    private WordleDailyService wordleDailyService;

    @Test
    void getTodaysWord_returnsWord_whenOneIsScheduledForToday() {
        WordleWord todays = new WordleWord("ბურთი", LocalDate.now(ZoneOffset.UTC), null);
        when(wordleWordRepository.findByScheduledDate(LocalDate.now(ZoneOffset.UTC)))
                .thenReturn(Optional.of(todays));

        Optional<WordleWord> result = wordleDailyService.getTodaysWord();

        assertTrue(result.isPresent());
        assertEquals("ბურთი", result.get().getWord());
    }

    @Test
    void getTodaysWord_returnsEmpty_whenNoWordScheduledForToday() {
        when(wordleWordRepository.findByScheduledDate(LocalDate.now(ZoneOffset.UTC)))
                .thenReturn(Optional.empty());

        Optional<WordleWord> result = wordleDailyService.getTodaysWord();

        assertTrue(result.isEmpty());
    }

    @Test
    void getTodaysWord_queriesByTodayInUtc() {
        when(wordleWordRepository.findByScheduledDate(any())).thenReturn(Optional.empty());

        wordleDailyService.getTodaysWord();

        verify(wordleWordRepository).findByScheduledDate(LocalDate.now(ZoneOffset.UTC));
    }
}
