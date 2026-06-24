package com.auracxeli.wordle;

import com.auracxeli.user.User;
import com.auracxeli.user.UserDetailsImpl;
import com.auracxeli.user.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Controller
public class WordleController {

    private static final int BOARD_ROWS = 6;

    private final WordleSessionService wordleSessionService;
    private final WordleDailyService wordleDailyService;
    private final WordleGuessEvaluator wordleGuessEvaluator;
    private final WordleGuessValidator wordleGuessValidator;
    private final UserRepository userRepository;

    public WordleController(WordleSessionService wordleSessionService,
                             WordleDailyService wordleDailyService,
                             WordleGuessEvaluator wordleGuessEvaluator,
                             WordleGuessValidator wordleGuessValidator,
                             UserRepository userRepository) {
        this.wordleSessionService = wordleSessionService;
        this.wordleDailyService = wordleDailyService;
        this.wordleGuessEvaluator = wordleGuessEvaluator;
        this.wordleGuessValidator = wordleGuessValidator;
        this.userRepository = userRepository;
    }

    @GetMapping("/wordle")
    public String showBoard(Authentication authentication, Model model) {
        Optional<WordleWord> todaysWord = wordleDailyService.getTodaysWord();
        if (todaysWord.isEmpty()) {
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
            model.addAttribute("noPuzzle", true);
            return "wordle";
        }

        User user = currentUser(authentication);
        WordleSession session = wordleSessionService.getOrCreateTodaysSession(user);
        try {
            if (!wordleGuessValidator.isValid(guess)) {
                throw new InvalidGeorgianWordException();
            }
            wordleSessionService.submitGuess(session, todaysWord.get(), guess);
        } catch (InvalidGeorgianWordException
                 | WordleSessionService.InvalidGuessException
                 | WordleSessionService.AlreadyCompletedException e) {
            populateBoardModel(model, session, todaysWord.get());
            model.addAttribute("error", e.getMessage());
            return "wordle";
        }

        return "redirect:/wordle";
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
