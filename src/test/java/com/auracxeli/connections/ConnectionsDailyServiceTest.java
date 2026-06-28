package com.auracxeli.connections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConnectionsDailyServiceTest {

    @Mock
    private ConnectionsPuzzleRepository connectionsPuzzleRepository;

    @Test
    void getTodaysPuzzle_returnsPuzzle_whenScheduledForToday() {
        ConnectionsDailyService service = new ConnectionsDailyService(connectionsPuzzleRepository);
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        ConnectionsPuzzle puzzle = new ConnectionsPuzzle(today);
        when(connectionsPuzzleRepository.findByPuzzleDate(today)).thenReturn(Optional.of(puzzle));
        Optional<ConnectionsPuzzle> result = service.getTodaysPuzzle();
        assertThat(result).isPresent();
        assertThat(result.get().getPuzzleDate()).isEqualTo(today);
    }

    @Test
    void getTodaysPuzzle_returnsEmpty_whenNoPuzzleScheduled() {
        ConnectionsDailyService service = new ConnectionsDailyService(connectionsPuzzleRepository);
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        when(connectionsPuzzleRepository.findByPuzzleDate(today)).thenReturn(Optional.empty());
        Optional<ConnectionsPuzzle> result = service.getTodaysPuzzle();
        assertThat(result).isEmpty();
    }

    @Test
    void getTodaysPuzzle_neverQueriesAnyDateOtherThanToday() {
        ConnectionsDailyService service = new ConnectionsDailyService(connectionsPuzzleRepository);
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        when(connectionsPuzzleRepository.findByPuzzleDate(any())).thenReturn(Optional.empty());
        service.getTodaysPuzzle();
        verify(connectionsPuzzleRepository).findByPuzzleDate(eq(today));
        verify(connectionsPuzzleRepository, never()).findByPuzzleDate(eq(today.plusDays(1)));
        verify(connectionsPuzzleRepository, never()).findByPuzzleDate(eq(today.minusDays(1)));
    }
}