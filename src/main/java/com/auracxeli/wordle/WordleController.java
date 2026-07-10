package com.auracxeli.wordle;

import com.auracxeli.user.UserDetailsImpl;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WordleController {

    private final WordleSessionService wordleSessionService;
    private final WordleDailyService wordleDailyService;
    private final WordleBoardService wordleBoardService;

    @GetMapping("/wordle")
    public String showBoard(@AuthenticationPrincipal UserDetailsImpl principal, Model model) {
        Optional<WordleWord> todaysWord = requireTodaysWord(model);
        if (todaysWord.isEmpty()) {
            return "wordle";
        }

        WordleSession session = wordleSessionService.getOrCreateTodaysSession(principal.getId());
        populateModel(model, wordleBoardService.buildBoard(session, todaysWord.get()));
        return "wordle";
    }

    @PostMapping("/wordle/guess")
    public String submitGuess(@AuthenticationPrincipal UserDetailsImpl principal,
                              @RequestParam(required = false, defaultValue = "") String guess,
                              Model model) {
        Optional<WordleWord> todaysWord = requireTodaysWord(model);
        if (todaysWord.isEmpty()) {
            return "wordle";
        }

        WordleSession session = wordleSessionService.getOrCreateTodaysSession(principal.getId());
        try {
            wordleSessionService.submitGuess(session, todaysWord.get(), guess);
        } catch (WordleSessionService.InvalidGuessException | WordleSessionService.AlreadyCompletedException
                 | InvalidGeorgianWordException e) {
            log.warn("Rejected guess for user {} session {}: {}", principal.getId(), session.getId(), e.getMessage());
            populateModel(model, wordleBoardService.buildBoard(session, todaysWord.get()));
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
    public String handleUnexpected(Exception ex, @AuthenticationPrincipal UserDetailsImpl principal, Model model) {
        Long userId = principal != null ? principal.getId() : null;
        log.error("Unexpected error handling Wordle request for user {}", userId, ex);
        model.addAttribute("error", "მოულოდნელი შეცდომა, სცადეთ მოგვიანებით");
        return "wordle";
    }

    /** Resolves today's word, flagging the "no puzzle scheduled" state on the model when absent. */
    private Optional<WordleWord> requireTodaysWord(Model model) {
        Optional<WordleWord> todaysWord = wordleDailyService.getTodaysWord();
        if (todaysWord.isEmpty()) {
            log.warn("No daily puzzle scheduled for {}", LocalDate.now(ZoneOffset.UTC));
            model.addAttribute("noPuzzle", true);
        }
        return todaysWord;
    }

    private void populateModel(Model model, WordleBoardView board) {
        model.addAttribute("rows", board.rows());
        model.addAttribute("outcome", board.outcome());
        model.addAttribute("readOnly", board.readOnly());
        model.addAttribute("attemptsUsed", board.attemptsUsed());
        model.addAttribute("maxAttempts", board.maxAttempts());
        if (board.revealedWord() != null) {
            model.addAttribute("revealedWord", board.revealedWord());
        }
    }
}
