package com.auracxeli.wordle;

import com.auracxeli.user.User;
import com.auracxeli.user.UserDetailsImpl;
import com.auracxeli.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Slf4j
@Controller
public class WordleController {

    private static final int BOARD_ROWS = 6;

    private final WordleSessionService wordleSessionService;
    private final WordleDailyService wordleDailyService;
    private final WordleGuessEvaluator wordleGuessEvaluator;
    private final UserRepository userRepository;

    public WordleController(WordleSessionService wordleSessionService,
                             WordleDailyService wordleDailyService,
                             WordleGuessEvaluator wordleGuessEvaluator,
                             UserRepository userRepository) {
        this.wordleSessionService = wordleSessionService;
        this.wordleDailyService = wordleDailyService;
        this.wordleGuessEvaluator = wordleGuessEvaluator;
        this.userRepository = userRepository;
    }

    @GetMapping("/wordle")
    public String showBoard(Authentication authentication, Model model) {
        Optional<WordleWord> todaysWord = wordleDailyService.getTodaysWord();
        if (todaysWord.isEmpty()) {
            log.warn("No daily puzzle scheduled for {}", LocalDate.now(ZoneOffset.UTC));
            model.addAttribute("noPuzzle", true);
            return "wordle";
        }

        User user = currentUser(authentication);
        WordleSession session = wordleSessionService.getOrCreateTodaysSession(user);
        populateBoardModel(model, session, todaysWord.get());
        return "wordle";
    }

    @PostMapping("/wordle/guess")
    public String submitGuess(Authentication authentication,
                               @RequestParam(required = false, defaultValue = "") String guess,
                               Model model) {
        Optional<WordleWord> todaysWord = wordleDailyService.getTodaysWord();
        if (todaysWord.isEmpty()) {
            log.warn("No daily puzzle scheduled for {}", LocalDate.now(ZoneOffset.UTC));
            model.addAttribute("noPuzzle", true);
            return "wordle";
        }

        User user = currentUser(authentication);
        WordleSession session = wordleSessionService.getOrCreateTodaysSession(user);
        try {
            wordleSessionService.submitGuess(session, todaysWord.get(), guess);
        } catch (WordleSessionService.InvalidGuessException | WordleSessionService.AlreadyCompletedException
                 | InvalidGeorgianWordException e) {
            log.warn("Rejected guess for user {} session {}: {}", user.getId(), session.getId(), e.getMessage());
            populateBoardModel(model, session, todaysWord.get());
            model.addAttribute("error", e.getMessage());
            return "wordle";
        }

        return "redirect:/wordle";
    }

    /**
     * Last-resort handler for unexpected failures during Wordle request handling
     * (e.g. the authenticated user vanishing, or a service-layer error). Logs the
     * exception with its stack trace and renders the board view with a generic error.
     */
    @ExceptionHandler(Exception.class)
    public String handleUnexpected(Exception ex, Authentication authentication, Model model) {
        Long userId = authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl principal
                ? principal.getId()
                : null;
        log.error("Unexpected error handling Wordle request for user {}", userId, ex);
        model.addAttribute("error", "მოულოდნელი შეცდომა, სცადეთ მოგვიანებით");
        return "wordle";
    }

    private User currentUser(Authentication authentication) {
        Long userId = ((UserDetailsImpl) authentication.getPrincipal()).getId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + userId));
    }

    private void populateBoardModel(Model model, WordleSession session, WordleWord todaysWord) {
        List<List<Tile>> rows = new ArrayList<>();
        for (WordleGuess guess : session.getGuesses()) {
            List<LetterFeedback> feedback = wordleGuessEvaluator.evaluateGuess(guess.getGuessWord(), todaysWord.getWord());
            rows.add(toTiles(guess.getGuessWord(), feedback));
        }
        while (rows.size() < BOARD_ROWS) {
            rows.add(blankRow());
        }

        boolean readOnly = session.getOutcome() != WordleOutcome.IN_PROGRESS;

        model.addAttribute("rows", rows);
        model.addAttribute("outcome", session.getOutcome());
        model.addAttribute("readOnly", readOnly);
        model.addAttribute("attemptsUsed", session.getGuesses().size());
        model.addAttribute("maxAttempts", BOARD_ROWS);
        if (session.getOutcome() == WordleOutcome.LOST) {
            model.addAttribute("revealedWord", todaysWord.getWord());
        }
    }

    private List<Tile> toTiles(String word, List<LetterFeedback> feedback) {
        List<Tile> tiles = new ArrayList<>();
        String upper = word.toUpperCase(Locale.ROOT);
        for (int i = 0; i < feedback.size(); i++) {
            tiles.add(new Tile(String.valueOf(upper.charAt(i)), cssClassFor(feedback.get(i))));
        }
        return tiles;
    }

    private List<Tile> blankRow() {
        List<Tile> tiles = new ArrayList<>();
        for (int i = 0; i < WordleGuessEvaluator.WORD_LENGTH; i++) {
            tiles.add(new Tile("", "t"));
        }
        return tiles;
    }

    private String cssClassFor(LetterFeedback feedback) {
        return switch (feedback) {
            case CORRECT -> "t g";
            case PRESENT -> "t y";
            case ABSENT -> "t x";
        };
    }

    public record Tile(String letter, String cssClass) {
    }
}
