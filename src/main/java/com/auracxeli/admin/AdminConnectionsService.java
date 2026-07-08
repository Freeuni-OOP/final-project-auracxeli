package com.auracxeli.admin;

import com.auracxeli.admin.dto.ConnectionsGroupRequest;
import com.auracxeli.admin.dto.CreateConnectionsPuzzleRequest;
import com.auracxeli.connections.ConnectionsGroup;
import com.auracxeli.connections.ConnectionsPuzzle;
import com.auracxeli.connections.ConnectionsPuzzleRepository;
import com.auracxeli.connections.ConnectionsWord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor

public class AdminConnectionsService {
    static final int WORDS_PER_GROUP = 4;
    static final int GROUPS_PER_PUZZLE = 4;
    private static final int UPCOMING_DAYS = 10;
    private final ConnectionsPuzzleRepository connectionsPuzzleRepository;

    // this method will return here the puzzles that are scheduled for today and after that too
    public List<ConnectionsPuzzle> upcomingPuzzles() {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        return connectionsPuzzleRepository.findByPuzzleDateGreaterThanEqualOrderByPuzzleDate(today);
    }

    // here I create the puzzle for corresponding groups and their words
    // it will throw DuplicatePuzzleDateException if the connection already exists for this date
    // or it will throw the illegal argument exception if the request doesnt have 4 groups and 4 words in each
    public ConnectionsPuzzle createPuzzle(CreateConnectionsPuzzleRequest request) {
        if (connectionsPuzzleRepository.existsByPuzzleDate(request.puzzleDate())) {
            log.warn("Rejected connections puzzle creation for {}: date already scheduled", request.puzzleDate());
            throw new DuplicatePuzzleDateException("puzzleDate", "ამ თარიღზე უკვე დაგეგმილია პაზლი");
        }

        List<ConnectionsGroupRequest> groups = request.groups();
        if (groups == null || groups.size() != GROUPS_PER_PUZZLE) {
            throw new IllegalArgumentException("პაზლს უნდა ჰქონდეს ზუსტად " + GROUPS_PER_PUZZLE + " ჯგუფი");
        }

        ConnectionsPuzzle puzzle = new ConnectionsPuzzle(request.puzzleDate());
        for (ConnectionsGroupRequest groupRequest : groups) {
            List<String> words = groupRequest.words();
            if (words == null || words.size() != WORDS_PER_GROUP) {
                throw new IllegalArgumentException("თითოეულ ჯგუფს უნდა ჰქონდეს ზუსტად " + WORDS_PER_GROUP + " სიტყვა");
            }

            ConnectionsGroup group = new ConnectionsGroup(puzzle, groupRequest.category(), groupRequest.difficulty());
            for (String word : words) {
                group.getWords().add(new ConnectionsWord(group, word));
            }
            puzzle.getGroups().add(group);
        }


        ConnectionsPuzzle saved = connectionsPuzzleRepository.save(puzzle);
        log.info("Scheduled connections puzzle for {}", saved.getPuzzleDate());
        return saved;


    }
}
