package com.auracxeli.connections;

import com.auracxeli.user.User;
import com.auracxeli.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class ConnectionsEntitiesTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @Autowired
    private ConnectionsPuzzleRepository puzzleRepository;

    @Autowired
    private ConnectionsGroupRepository groupRepository;

    @Autowired
    private ConnectionsWordRepository wordRepository;

    @Autowired
    private ConnectionsSessionRepository sessionRepository;

    @Autowired
    private ConnectionsGuessRepository guessRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = userRepository.save(new User("testuser", "test@example.com", "hash"));
    }

    @Test
    void save_puzzleWithGroupsAndWords_persistsCorrectly() {
        ConnectionsPuzzle puzzle = new ConnectionsPuzzle(LocalDate.now());
        puzzle = puzzleRepository.save(puzzle);
        ConnectionsGroup group = new ConnectionsGroup(puzzle, "მულტფილმები", 1);
        group = groupRepository.save(group);
        ConnectionsWord word = new ConnectionsWord(group, "სპანჯბობი");
        wordRepository.save(word);
        Optional<ConnectionsPuzzle> found = puzzleRepository.findByPuzzleDate(puzzle.getPuzzleDate());
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(puzzle.getId());
    }

    @Test
    void save_sessionWithGuess_persistsAndCascades() {
        ConnectionsSession session = new ConnectionsSession(testUser, LocalDate.now());
        session = sessionRepository.save(session);
        ConnectionsGuess guess = new ConnectionsGuess(session, "ვ", "ბ", "ც", "მ", true, 1);
        session.getGuesses().add(guess);
        sessionRepository.save(session);
        Optional<ConnectionsSession> found = sessionRepository.findByPuzzleDateAndUserId(session.getPuzzleDate(), testUser.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getGuesses()).hasSize(1);
    }
}