package com.auracxeli.admin;

import com.auracxeli.admin.dto.ConnectionsGroupRequest;
import com.auracxeli.admin.dto.CreateConnectionsPuzzleRequest;
import com.auracxeli.admin.dto.ScheduledPuzzle;
import com.auracxeli.connections.ConnectionsPuzzle;
import com.auracxeli.connections.ConnectionsPuzzleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.ZoneOffset;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminConnectionsServiceTest {

    @Mock
    private ConnectionsPuzzleRepository connectionsPuzzleRepository;

    @InjectMocks
    private AdminConnectionsService adminConnectionsService;

    private final LocalDate date = LocalDate.now().plusDays(3);

    private static ConnectionsGroupRequest group(String category, int difficulty, String... words) {
        return new ConnectionsGroupRequest(category, difficulty, List.of(words));
    }

    private CreateConnectionsPuzzleRequest validRequest() {
        return new CreateConnectionsPuzzleRequest(date, List.of(
                group("ხილი", 1, "ვაშლი", "მსხალი", "ატამი", "ბანანი"),
                group("ცხოველი", 2, "ძაღლი", "კატა", "ცხენი", "ლომი"),
                group("ფერი", 3, "წითელი", "ლურჯი", "მწვანე", "ყვითელი"),
                group("ქალაქი", 4, "თბილისი", "ბათუმი", "ქუთაისი", "ზუგდიდი")
        ));
    }

    @Test
    void createPuzzle_happyPath_savesPuzzleWithGroupsAndWords() {
        when(connectionsPuzzleRepository.existsByPuzzleDate(date)).thenReturn(false);
        when(connectionsPuzzleRepository.save(any(ConnectionsPuzzle.class))).thenAnswer(inv -> inv.getArgument(0));

        ScheduledPuzzle result = adminConnectionsService.createPuzzle(validRequest());

        ArgumentCaptor<ConnectionsPuzzle> captor = ArgumentCaptor.forClass(ConnectionsPuzzle.class);
        verify(connectionsPuzzleRepository).save(captor.capture());
        ConnectionsPuzzle saved = captor.getValue();

        assertEquals(date, saved.getPuzzleDate());
        assertEquals(4, saved.getGroups().size());
        saved.getGroups().forEach(g -> assertEquals(4, g.getWords().size()));
        assertEquals(date, result.puzzleDate());
        assertEquals(4, result.groups().size());
    }

    @Test
    void createPuzzle_duplicateDate_throwsAndDoesNotSave() {
        when(connectionsPuzzleRepository.existsByPuzzleDate(date)).thenReturn(true);

        assertThrows(DuplicatePuzzleDateException.class, () -> adminConnectionsService.createPuzzle(validRequest()));
        verify(connectionsPuzzleRepository, never()).save(any());
    }

    @Test
    void createPuzzle_groupWithWrongWordCount_throwsAndDoesNotSave() {
        when(connectionsPuzzleRepository.existsByPuzzleDate(date)).thenReturn(false);

        CreateConnectionsPuzzleRequest request = new CreateConnectionsPuzzleRequest(date, List.of(
                group("ხილი", 1, "ვაშლი", "მსხალი", "ატამი"),
                group("ცხოველი", 2, "ძაღლი", "კატა", "ცხენი", "ლომი"),
                group("ფერი", 3, "წითელი", "ლურჯი", "მწვანე", "ყვითელი"),
                group("ქალაქი", 4, "თბილისი", "ბათუმი", "ქუთაისი", "ზუგდიდი")
        ));

        assertThrows(IllegalArgumentException.class, () -> adminConnectionsService.createPuzzle(request));
        verify(connectionsPuzzleRepository, never()).save(any());
    }
    @Test
    void upcomingPuzzles_queriesFromToday() {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        when(connectionsPuzzleRepository.findByPuzzleDateGreaterThanEqualOrderByPuzzleDate(today)).thenReturn(List.of());

        adminConnectionsService.upcomingPuzzles();
        verify(connectionsPuzzleRepository).findByPuzzleDateGreaterThanEqualOrderByPuzzleDate(today);
    }
}