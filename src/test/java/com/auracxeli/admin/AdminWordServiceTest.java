package com.auracxeli.admin;

import com.auracxeli.wordle.InvalidGeorgianWordException;
import com.auracxeli.wordle.WordleGuessValidator;
import com.auracxeli.wordle.WordleWord;
import com.auracxeli.wordle.WordleWordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminWordServiceTest {

    @Mock
    private WordleWordRepository wordleWordRepository;

    @Mock
    private WordleGuessValidator wordleGuessValidator;

    @InjectMocks
    private AdminWordService adminWordService;

    private final LocalDate today = LocalDate.now(ZoneOffset.UTC);

    @Test
    void blankDate_slotsIntoFirstFreeDay() {
        when(wordleGuessValidator.isValid("ბურთი")).thenReturn(true);
        // today is taken, today+1 is free -> the word should land on today+1
        when(wordleWordRepository.existsByScheduledDate(today)).thenReturn(true);
        when(wordleWordRepository.existsByScheduledDate(today.plusDays(1))).thenReturn(false);
        when(wordleWordRepository.existsByWordAndScheduledDateBetween(eq("ბურთი"), any(), any()))
                .thenReturn(false);
        when(wordleWordRepository.save(any(WordleWord.class))).thenAnswer(inv -> inv.getArgument(0));

        adminWordService.addWord("ბურთი", null, 1L);

        ArgumentCaptor<WordleWord> captor = ArgumentCaptor.forClass(WordleWord.class);
        verify(wordleWordRepository).save(captor.capture());
        assertEquals(today.plusDays(1), captor.getValue().getScheduledDate());
    }

    @Test
    void blankDate_uniquenessConflict_throwsAndDoesNotSave() {
        when(wordleGuessValidator.isValid("ბურთი")).thenReturn(true);
        when(wordleWordRepository.existsByScheduledDate(today)).thenReturn(false); // first free = today
        when(wordleWordRepository.existsByWordAndScheduledDateBetween(eq("ბურთი"), any(), any()))
                .thenReturn(true);

        assertThrows(DuplicateWordException.class, () -> adminWordService.addWord("ბურთი", null, 1L));
        verify(wordleWordRepository, never()).save(any());
    }

    @Test
    void givenFreeDate_usesThatDate() {
        when(wordleGuessValidator.isValid("ბურთი")).thenReturn(true);
        LocalDate requested = today.plusDays(5);
        when(wordleWordRepository.existsByScheduledDate(requested)).thenReturn(false);
        when(wordleWordRepository.existsByWordAndScheduledDateBetween(eq("ბურთი"), any(), any()))
                .thenReturn(false);
        when(wordleWordRepository.save(any(WordleWord.class))).thenAnswer(inv -> inv.getArgument(0));

        adminWordService.addWord("ბურთი", requested, 1L);

        ArgumentCaptor<WordleWord> captor = ArgumentCaptor.forClass(WordleWord.class);
        verify(wordleWordRepository).save(captor.capture());
        assertEquals(requested, captor.getValue().getScheduledDate());
    }

    @Test
    void givenTakenDate_fallsBackToFirstFreeDay() {
        when(wordleGuessValidator.isValid("ბურთი")).thenReturn(true);
        LocalDate requested = today.plusDays(5);
        when(wordleWordRepository.existsByScheduledDate(requested)).thenReturn(true);  // requested day taken
        when(wordleWordRepository.existsByScheduledDate(today)).thenReturn(false);     // first free = today
        when(wordleWordRepository.existsByWordAndScheduledDateBetween(eq("ბურთი"), any(), any()))
                .thenReturn(false);
        when(wordleWordRepository.save(any(WordleWord.class))).thenAnswer(inv -> inv.getArgument(0));

        adminWordService.addWord("ბურთი", requested, 1L);

        ArgumentCaptor<WordleWord> captor = ArgumentCaptor.forClass(WordleWord.class);
        verify(wordleWordRepository).save(captor.capture());
        assertEquals(today, captor.getValue().getScheduledDate()); // fell back, not the requested day
    }

    @Test
    void givenFreeDate_uniquenessConflict_throws() {
        when(wordleGuessValidator.isValid("ბურთი")).thenReturn(true);
        LocalDate requested = today.plusDays(5);
        when(wordleWordRepository.existsByScheduledDate(requested)).thenReturn(false);
        when(wordleWordRepository.existsByWordAndScheduledDateBetween(eq("ბურთი"), any(), any()))
                .thenReturn(true);

        assertThrows(DuplicateWordException.class, () -> adminWordService.addWord("ბურთი", requested, 1L));
        verify(wordleWordRepository, never()).save(any());
    }

    @Test
    void addWord_rejectsWordNotInDictionary() {
        when(wordleGuessValidator.isValid("ააააა")).thenReturn(false);

        assertThrows(InvalidGeorgianWordException.class,
                () -> adminWordService.addWord("ააააა", null, 1L));
        verify(wordleWordRepository, never()).save(any());
    }

    @Test
    void upcomingWords_queriesNextTenDays() {
        when(wordleWordRepository.findByScheduledDateBetweenOrderByScheduledDate(any(), any()))
                .thenReturn(List.of());

        adminWordService.upcomingWords();

        verify(wordleWordRepository).findByScheduledDateBetweenOrderByScheduledDate(today, today.plusDays(9));
    }
}
