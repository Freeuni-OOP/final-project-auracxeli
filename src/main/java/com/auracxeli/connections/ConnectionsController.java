package com.auracxeli.connections;

import com.auracxeli.user.User;
import com.auracxeli.user.UserDetailsImpl;
import com.auracxeli.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ConnectionsController {

    // must match ConnectionsSessionService.MAX_MISTAKES
    private static final int MAX_MISTAKES = 4;

    private final ConnectionsSessionService connectionsSessionService;
    private final ConnectionsDailyService connectionsDailyService;
    private final ConnectionsGuessEvaluator connectionsGuessEvaluator;
    private final UserRepository userRepository;

    @GetMapping("/connections")
    public String showBoard(Authentication authentication, Model model) {
        Optional<ConnectionsPuzzle> todaysPuzzle = connectionsDailyService.getTodaysPuzzle();
        if (todaysPuzzle.isEmpty()) {
            log.warn("No Connections puzzle scheduled for {}", LocalDate.now(ZoneOffset.UTC));
            model.addAttribute("noPuzzle", true);
            return "connections";
        }

        User user = currentUser(authentication);
        ConnectionsSession session = connectionsSessionService.getOrCreateTodaysSession(user);
        populateBoardModel(model, session, todaysPuzzle.get());
        return "connections";
    }

    @PostMapping("/connections/guess")
    public String submitGuess(Authentication authentication,
                              @RequestParam(required = false) List<String> words,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        Optional<ConnectionsPuzzle> todaysPuzzle = connectionsDailyService.getTodaysPuzzle();
        if (todaysPuzzle.isEmpty()) {
            log.warn("No Connections puzzle scheduled for {}", LocalDate.now(ZoneOffset.UTC));
            model.addAttribute("noPuzzle", true);
            return "connections";
        }

        User user = currentUser(authentication);
        ConnectionsSession session = connectionsSessionService.getOrCreateTodaysSession(user);
        Set<String> selection = words == null ? null : new HashSet<>(words);

        try {
            ConnectionsSession updated = connectionsSessionService.submitGuess(session, todaysPuzzle.get(), selection);
            ConnectionsGuess lastGuess = updated.getGuesses().get(updated.getGuesses().size() - 1);
            String resultType = classifyGuess(lastGuess, todaysPuzzle.get());
            redirectAttributes.addFlashAttribute("lastGuessResult", resultType);
            if (!"correct".equals(resultType)) {
                redirectAttributes.addFlashAttribute("lastGuessWords",
                        List.of(lastGuess.getWord1(), lastGuess.getWord2(), lastGuess.getWord3(), lastGuess.getWord4()));
            }
        } catch (InvalidSelectionException | AlreadyCompletedException e) {
            log.warn("Rejected Connections guess for user {} session {}: {}",
                    user.getId(), session.getId(), e.getMessage());
            populateBoardModel(model, session, todaysPuzzle.get());
            model.addAttribute("error", "არასწორი მონიშვნა, სცადეთ თავიდან");
            return "connections";
        }

        return "redirect:/connections";
    }

    /** "correct", "one_away" (3 of 4 words match some group), or "wrong". */
    private String classifyGuess(ConnectionsGuess guess, ConnectionsPuzzle puzzle) {
        if (guess.isCorrect()) {
            return "correct";
        }
        List<String> words = List.of(guess.getWord1(), guess.getWord2(), guess.getWord3(), guess.getWord4());
        for (ConnectionsGroup group : puzzle.getGroups()) {
            if (connectionsGuessEvaluator.isAlmostCorrect(words, group)) {
                return "one_away";
            }
        }
        return "wrong";
    }

    /**
     * Last-resort handler for unexpected failures during Connections request handling,
     * mirroring WordleController's pattern.
     */
    @ExceptionHandler(Exception.class)
    public String handleUnexpected(Exception ex, Authentication authentication, Model model) {
        Long userId = authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl principal
                ? principal.getId()
                : null;
        log.error("Unexpected error handling Connections request for user {}", userId, ex);
        model.addAttribute("error", "მოულოდნელი შეცდომა, სცადეთ მოგვიანებით");
        return "connections";
    }

    private User currentUser(Authentication authentication) {
        Long userId = ((UserDetailsImpl) authentication.getPrincipal()).getId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + userId));
    }

    private void populateBoardModel(Model model, ConnectionsSession session, ConnectionsPuzzle puzzle) {
        boolean readOnly = session.getOutcome() != ConnectionsOutcome.IN_PROGRESS;

        List<ConnectionsBoardView.GroupView> solvedGroups = new ArrayList<>();
        Set<Long> solvedGroupIds = new HashSet<>();

        for (ConnectionsGuess guess : session.getGuesses()) {
            if (!guess.isCorrect()) {
                continue;
            }
            List<String> guessedWords = List.of(guess.getWord1(), guess.getWord2(), guess.getWord3(), guess.getWord4());
            connectionsGuessEvaluator.matchingGroup(guessedWords, puzzle).ifPresent(group -> {
                solvedGroupIds.add(group.getId());
                solvedGroups.add(toGroupView(group));
            });
        }

        List<String> remainingWords = shuffledUnsolvedWords(session, puzzle, solvedGroupIds);

        List<ConnectionsBoardView.GroupView> revealedGroups = new ArrayList<>();
        if (session.getOutcome() == ConnectionsOutcome.LOST) {
            for (ConnectionsGroup group : puzzle.getGroups()) {
                if (!solvedGroupIds.contains(group.getId())) {
                    revealedGroups.add(toGroupView(group));
                }
            }
        }

        ConnectionsBoardView view = new ConnectionsBoardView(
                solvedGroups,
                remainingWords,
                session.getMistakesCount(),
                MAX_MISTAKES,
                session.getOutcome(),
                readOnly,
                revealedGroups
        );

        model.addAttribute("board", view);
    }

    private ConnectionsBoardView.GroupView toGroupView(ConnectionsGroup group) {
        List<String> words = group.getWords().stream().map(ConnectionsWord::getWord).toList();
        return new ConnectionsBoardView.GroupView(group.getCategory(), group.getDifficulty(), words);
    }

    private List<String> shuffledUnsolvedWords(ConnectionsSession session, ConnectionsPuzzle puzzle, Set<Long> solvedGroupIds) {
        List<String> allWords = new ArrayList<>();
        List<ConnectionsGroup> groups = puzzle.getGroups();
        for (ConnectionsGroup group : groups) {
            for (ConnectionsWord word : group.getWords()) {
                allWords.add(word.getWord());
            }
        }
        Collections.shuffle(allWords, new Random(session.getId()));

        Set<String> solvedWords = new HashSet<>();
        for (ConnectionsGroup group : groups) {
            if (solvedGroupIds.contains(group.getId())) {
                group.getWords().forEach(w -> solvedWords.add(w.getWord()));
            }
        }

        List<String> remaining = new ArrayList<>();
        for (String word : allWords) {
            if (!solvedWords.contains(word)) {
                remaining.add(word);
            }
        }
        return remaining;
    }
}