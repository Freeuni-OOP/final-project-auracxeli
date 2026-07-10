package com.auracxeli.archive;

import com.auracxeli.connections.ConnectionsBoardView;
import com.auracxeli.connections.ConnectionsDailyService;
import com.auracxeli.connections.ConnectionsPuzzle;
import com.auracxeli.connections.ConnectionsPuzzleRepository;
import com.auracxeli.wordle.WordleDailyService;
import com.auracxeli.wordle.WordleWord;
import com.auracxeli.wordle.WordleWordRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * The archive: browse past dates and practise any of them. Practice games run
 * through {@link PracticeService} with their state held in the HTTP session, so
 * they never touch the persisted daily sessions, stats, streaks or achievements.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/archive")
public class ArchiveController {

    private final WordleDailyService wordleDailyService;
    private final ConnectionsDailyService connectionsDailyService;
    private final WordleWordRepository wordleWordRepository;
    private final ConnectionsPuzzleRepository connectionsPuzzleRepository;
    private final PracticeService practiceService;

    // ===================== Wordle =====================

    @GetMapping("/wordle")
    public String wordleArchive(Model model) {
        List<LocalDate> dates = wordleWordRepository
                .findByScheduledDateLessThanOrderByScheduledDateDesc(today())
                .stream().map(WordleWord::getScheduledDate).toList();
        model.addAttribute("gameTitle", "Wordle");
        model.addAttribute("basePath", "/archive/wordle");
        model.addAttribute("dates", dates);
        model.addAttribute("activePage", "archive");
        return "archive";
    }

    @GetMapping("/wordle/{date}")
    public String playWordle(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                             HttpSession session, Model model) {
        Optional<WordleWord> word = playableWord(date);
        if (word.isEmpty()) {
            return "redirect:/archive/wordle";
        }
        PracticeService.WordlePracticeView board = practiceService.buildWordleBoard(wordleGuesses(session, date), word.get());
        populateWordle(model, board, date);
        return "wordle";
    }

    @PostMapping("/wordle/{date}/guess")
    public String guessWordle(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                              @RequestParam(required = false, defaultValue = "") String guess,
                              HttpSession session, Model model) {
        Optional<WordleWord> word = playableWord(date);
        if (word.isEmpty()) {
            return "redirect:/archive/wordle";
        }
        List<String> guesses = wordleGuesses(session, date);
        PracticeService.WordlePracticeView board = practiceService.buildWordleBoard(guesses, word.get());
        if (board.readOnly()) {
            return "redirect:/archive/wordle/" + date;
        }
        try {
            practiceService.validateWordleGuess(guess);
        } catch (PracticeService.InvalidPracticeGuessException e) {
            populateWordle(model, board, date);
            model.addAttribute("error", "არასწორი სიტყვა");
            return "wordle";
        }
        guesses.add(guess.trim().toLowerCase(Locale.ROOT));
        session.setAttribute(wordleKey(date), guesses);
        return "redirect:/archive/wordle/" + date;
    }

    @GetMapping("/wordle/{date}/restart")
    public String restartWordle(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                HttpSession session) {
        session.removeAttribute(wordleKey(date));
        return "redirect:/archive/wordle/" + date;
    }

    private Optional<WordleWord> playableWord(LocalDate date) {
        // Only strictly-past dates are practisable; today stays the once-only daily game.
        if (!date.isBefore(today())) {
            return Optional.empty();
        }
        return wordleDailyService.getWordForDate(date);
    }

    private void populateWordle(Model model, PracticeService.WordlePracticeView board, LocalDate date) {
        model.addAttribute("rows", board.rows());
        model.addAttribute("outcome", board.outcome());
        model.addAttribute("readOnly", board.readOnly());
        model.addAttribute("attemptsUsed", board.attemptsUsed());
        model.addAttribute("maxAttempts", board.maxAttempts());
        if (board.revealedWord() != null) {
            model.addAttribute("revealedWord", board.revealedWord());
        }
        model.addAttribute("practiceDate", date);
    }

    @SuppressWarnings("unchecked")
    private List<String> wordleGuesses(HttpSession session, LocalDate date) {
        Object stored = session.getAttribute(wordleKey(date));
        return stored instanceof List ? new ArrayList<>((List<String>) stored) : new ArrayList<>();
    }

    private String wordleKey(LocalDate date) {
        return "practice.wordle." + date;
    }

    // ===================== Connections =====================

    @GetMapping("/connections")
    public String connectionsArchive(Model model) {
        List<LocalDate> dates = connectionsPuzzleRepository
                .findByPuzzleDateLessThanOrderByPuzzleDateDesc(today())
                .stream().map(ConnectionsPuzzle::getPuzzleDate).toList();
        model.addAttribute("gameTitle", "Connections");
        model.addAttribute("basePath", "/archive/connections");
        model.addAttribute("dates", dates);
        model.addAttribute("activePage", "archive");
        return "archive";
    }

    @GetMapping("/connections/{date}")
    public String playConnections(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                  HttpSession session, Model model) {
        Optional<ConnectionsPuzzle> puzzle = playablePuzzle(date);
        if (puzzle.isEmpty()) {
            return "redirect:/archive/connections";
        }
        ConnectionsBoardView board = practiceService.buildConnectionsBoard(connectionsGuesses(session, date), puzzle.get());
        populateConnections(model, board, date);
        return "connections";
    }

    @PostMapping("/connections/{date}/guess")
    public String guessConnections(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                   @RequestParam(required = false) List<String> words,
                                   HttpSession session, RedirectAttributes redirectAttributes, Model model) {
        Optional<ConnectionsPuzzle> puzzle = playablePuzzle(date);
        if (puzzle.isEmpty()) {
            return "redirect:/archive/connections";
        }
        List<List<String>> guesses = connectionsGuesses(session, date);
        ConnectionsBoardView board = practiceService.buildConnectionsBoard(guesses, puzzle.get());
        if (board.readOnly()) {
            return "redirect:/archive/connections/" + date;
        }
        List<String> selection = words == null ? null : new ArrayList<>(words);
        try {
            practiceService.validateConnectionsSelection(selection);
        } catch (PracticeService.InvalidPracticeGuessException e) {
            populateConnections(model, board, date);
            model.addAttribute("error", "არასწორი მონიშვნა, სცადეთ თავიდან");
            return "connections";
        }

        String result = practiceService.classifyConnectionsGuess(selection, puzzle.get());
        redirectAttributes.addFlashAttribute("lastGuessResult", result);
        if (!"correct".equals(result)) {
            redirectAttributes.addFlashAttribute("lastGuessWords", selection);
        }
        guesses.add(selection);
        session.setAttribute(connectionsKey(date), guesses);
        return "redirect:/archive/connections/" + date;
    }

    @GetMapping("/connections/{date}/restart")
    public String restartConnections(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                     HttpSession session) {
        session.removeAttribute(connectionsKey(date));
        return "redirect:/archive/connections/" + date;
    }

    private Optional<ConnectionsPuzzle> playablePuzzle(LocalDate date) {
        // Only strictly-past dates are practisable; today stays the once-only daily game.
        if (!date.isBefore(today())) {
            return Optional.empty();
        }
        return connectionsDailyService.getPuzzleForDate(date);
    }

    private void populateConnections(Model model, ConnectionsBoardView board, LocalDate date) {
        model.addAttribute("board", board);
        model.addAttribute("practiceDate", date);
    }

    @SuppressWarnings("unchecked")
    private List<List<String>> connectionsGuesses(HttpSession session, LocalDate date) {
        Object stored = session.getAttribute(connectionsKey(date));
        return stored instanceof List ? new ArrayList<>((List<List<String>>) stored) : new ArrayList<>();
    }

    private String connectionsKey(LocalDate date) {
        return "practice.connections." + date;
    }

    // ===================== shared =====================

    private LocalDate today() {
        return LocalDate.now(ZoneOffset.UTC);
    }
}
