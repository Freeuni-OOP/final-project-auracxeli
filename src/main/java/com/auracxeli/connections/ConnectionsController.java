package com.auracxeli.connections;

import com.auracxeli.user.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ConnectionsController {

    private final ConnectionsSessionService connectionsSessionService;
    private final ConnectionsDailyService connectionsDailyService;
    private final ConnectionsBoardService connectionsBoardService;

    @GetMapping("/connections")
    public String showBoard(@AuthenticationPrincipal UserDetailsImpl principal, Model model) {
        Optional<ConnectionsPuzzle> todaysPuzzle = requireTodaysPuzzle(model);
        if (todaysPuzzle.isEmpty()) {
            return "connections";
        }

        ConnectionsSession session = connectionsSessionService.getOrCreateTodaysSession(principal.getId());
        model.addAttribute("board", connectionsBoardService.buildBoard(session, todaysPuzzle.get()));
        return "connections";
    }

    @PostMapping("/connections/guess")
    public String submitGuess(@AuthenticationPrincipal UserDetailsImpl principal,
                              @RequestParam(required = false) List<String> words,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        Optional<ConnectionsPuzzle> todaysPuzzle = requireTodaysPuzzle(model);
        if (todaysPuzzle.isEmpty()) {
            return "connections";
        }

        ConnectionsSession session = connectionsSessionService.getOrCreateTodaysSession(principal.getId());
        Set<String> selection = words == null ? null : new HashSet<>(words);

        try {
            ConnectionsSession updated = connectionsSessionService.submitGuess(session, todaysPuzzle.get(), selection);
            ConnectionsGuess lastGuess = updated.getGuesses().getLast();
            String resultType = connectionsBoardService.classifyGuess(lastGuess, todaysPuzzle.get());
            redirectAttributes.addFlashAttribute("lastGuessResult", resultType);
            if (!"correct".equals(resultType)) {
                redirectAttributes.addFlashAttribute("lastGuessWords",
                        List.of(lastGuess.getWord1(), lastGuess.getWord2(), lastGuess.getWord3(), lastGuess.getWord4()));
            }
        } catch (InvalidSelectionException | AlreadyCompletedException e) {
            log.warn("Rejected Connections guess for user {} session {}: {}",
                    principal.getId(), session.getId(), e.getMessage());
            model.addAttribute("board", connectionsBoardService.buildBoard(session, todaysPuzzle.get()));
            model.addAttribute("error", "არასწორი მონიშვნა, სცადეთ თავიდან");
            return "connections";
        }

        return "redirect:/connections";
    }

    /**
     * Last-resort handler for unexpected failures during Connections request handling,
     * mirroring WordleController's pattern.
     */
    @ExceptionHandler(Exception.class)
    public String handleUnexpected(Exception ex, @AuthenticationPrincipal UserDetailsImpl principal, Model model) {
        Long userId = principal != null ? principal.getId() : null;
        log.error("Unexpected error handling Connections request for user {}", userId, ex);
        model.addAttribute("error", "მოულოდნელი შეცდომა, სცადეთ მოგვიანებით");
        return "connections";
    }

    /** Resolves today's puzzle, flagging the "no puzzle scheduled" state on the model when absent. */
    private Optional<ConnectionsPuzzle> requireTodaysPuzzle(Model model) {
        Optional<ConnectionsPuzzle> todaysPuzzle = connectionsDailyService.getTodaysPuzzle();
        if (todaysPuzzle.isEmpty()) {
            log.warn("No Connections puzzle scheduled for {}", LocalDate.now(ZoneOffset.UTC));
            model.addAttribute("noPuzzle", true);
        }
        return todaysPuzzle;
    }
}
