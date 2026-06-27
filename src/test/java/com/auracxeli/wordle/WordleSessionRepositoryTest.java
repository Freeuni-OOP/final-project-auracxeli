package com.auracxeli.wordle;

import com.auracxeli.user.User;
import com.auracxeli.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataAccessException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * I use a real mySQL containers via testcontainers to test the methods WORdleGUess,WordleSession,
 * WordleOutcome and WordleSessionRepository
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class WordleSessionRepositoryTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }
    @Autowired
    private WordleSessionRepository wordleSessionRepository;
    @Autowired
    private UserRepository userRepository;
    private User testUser;
    @BeforeEach
    void setUp() {
        testUser = userRepository.save(
                new User("testuser", "testuser@example.com", "dummy-hashed-password")
        );
    }
    @Test
    void save_thenFindByUserIdAndPuzzleDate_returnsSession() {
        LocalDate puzzleDate = LocalDate.now();
        WordleSession session = new WordleSession(testUser, puzzleDate);
        wordleSessionRepository.save(session);

        Optional<WordleSession> found =
                wordleSessionRepository.findByUserIdAndPuzzleDate(testUser.getId(), puzzleDate);

        assertThat(found).isPresent();
        assertThat(found.get().getPuzzleDate()).isEqualTo(puzzleDate);
        assertThat(found.get().getOutcome()).isEqualTo(WordleOutcome.IN_PROGRESS);
        assertThat(found.get().getUser().getId()).isEqualTo(testUser.getId());
    }
    @Test
    void findByUserIdAndPuzzleDate_returnsEmpty_whenNoSessionExists() {
        Optional<WordleSession> found =
                wordleSessionRepository.findByUserIdAndPuzzleDate(testUser.getId(), LocalDate.now());

        assertThat(found).isEmpty();
    }
    @Test
    void save_persistsGuessesViaCascade() {
        WordleSession session = new WordleSession(testUser, LocalDate.now());
        session.getGuesses().add(new WordleGuess(session, "ვარდი", 1));
        session.getGuesses().add(new WordleGuess(session, "ბარგი", 2));
        WordleSession saved = wordleSessionRepository.save(session);
        Optional<WordleSession> found =
                wordleSessionRepository.findByUserIdAndPuzzleDate(testUser.getId(), saved.getPuzzleDate());
        assertThat(found).isPresent();
        assertThat(found.get().getGuesses()).hasSize(2);
        assertThat(found.get().getGuesses())
                .extracting(WordleGuess::getGuessWord)
                .containsExactlyInAnyOrder("ვარდი", "ბარგი");
        // I test the getters here
        WordleGuess firstGuess = found.get().getGuesses().get(0);
        assertThat(firstGuess.getId()).isNotNull();
        assertThat(firstGuess.getSession().getId()).isEqualTo(saved.getId());
        assertThat(firstGuess.getGuessNumber()).isIn((short) 1, (short) 2);
        assertThat(firstGuess.getCreatedAt()).isNotNull();
    }
    @Test
    void setOutcome_updatesOutcomeAfterSave() {
        WordleSession session = new WordleSession(testUser, LocalDate.now());
        WordleSession saved = wordleSessionRepository.save(session);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        saved.setOutcome(WordleOutcome.WON);
        wordleSessionRepository.save(saved);
        Optional<WordleSession> reloaded =
                wordleSessionRepository.findByUserIdAndPuzzleDate(testUser.getId(), saved.getPuzzleDate());
        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().getOutcome()).isEqualTo(WordleOutcome.WON);
    }

    @Test
    void findByUserIdOrderByPuzzleDateAsc_returnsSessionsOldestFirst() {
        wordleSessionRepository.save(new WordleSession(testUser, LocalDate.of(2026, 1, 3)));
        wordleSessionRepository.save(new WordleSession(testUser, LocalDate.of(2026, 1, 1)));
        wordleSessionRepository.save(new WordleSession(testUser, LocalDate.of(2026, 1, 2)));

        List<WordleSession> result =
                wordleSessionRepository.findByUserIdOrderByPuzzleDateAsc(testUser.getId());
        assertThat(result)
                .extracting(WordleSession::getPuzzleDate)
                .containsExactly(
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 1, 2),
                        LocalDate.of(2026, 1, 3));
    }

    @Test
    void saveAndFlush_rejectsGuessNumberAboveSix() {
        // Wordle allows only 6 guesses, so a 7th row must be rejected at the DB level (issue #34).
        WordleSession session = new WordleSession(testUser, LocalDate.now());
        session.getGuesses().add(new WordleGuess(session, "ვარდი", 7));

        assertThatThrownBy(() -> wordleSessionRepository.saveAndFlush(session))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("chk_guess_number");
    }

    @Test
    void findGuessDistribution_bucketsWonGamesByGuessCount() {
        saveWonSession(LocalDate.of(2026, 2, 1), 3);
        saveWonSession(LocalDate.of(2026, 2, 2), 3);
        saveWonSession(LocalDate.of(2026, 2, 3), 5);
        saveLostSession(LocalDate.of(2026, 2, 4), 6); // losses must not be counted

        Map<Integer, Integer> games = new HashMap<>();
        for (Object[] row : wordleSessionRepository.findGuessDistribution(testUser.getId())) {
            games.put(((Number) row[0]).intValue(), ((Number) row[1]).intValue());
        }

        assertThat(games).containsEntry(3, 2).containsEntry(5, 1);
        assertThat(games).doesNotContainKey(6); // the lost game is excluded
    }

    private void saveWonSession(LocalDate date, int guesses) {
        saveSession(date, guesses, WordleOutcome.WON);
    }

    private void saveLostSession(LocalDate date, int guesses) {
        saveSession(date, guesses, WordleOutcome.LOST);
    }

    private void saveSession(LocalDate date, int guesses, WordleOutcome outcome) {
        WordleSession session = new WordleSession(testUser, date);
        for (int n = 1; n <= guesses; n++) {
            session.getGuesses().add(new WordleGuess(session, "ვარდი", n));
        }
        session.setOutcome(outcome);
        wordleSessionRepository.save(session);
    }

}