package com.auracxeli.admin;

import com.auracxeli.admin.dto.ConnectionsGroupRequest;
import com.auracxeli.admin.dto.CreateConnectionsPuzzleRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/admin/connections")
@RequiredArgsConstructor
public class AdminConnectionsController {
    private final AdminConnectionsService adminConnectionsService;
    @GetMapping
    public String showPuzzles(Model model) {
        model.addAttribute("createConnectionsPuzzleRequest", emptyRequest());
        model.addAttribute("puzzles", adminConnectionsService.upcomingPuzzles());
        return "admin/connections";
    }


    @PostMapping
    public String createPuzzle(@Valid @ModelAttribute("createConnectionsPuzzleRequest") CreateConnectionsPuzzleRequest request, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        if (bindingResult.hasErrors()) {
            log.warn("Connections puzzle submission rejected: {} validation error(s)", bindingResult.getErrorCount());
        } else {
            try {
                var saved = adminConnectionsService.createPuzzle(request);
                redirectAttributes.addFlashAttribute("message",
                        "პაზლი დაემატა " + saved.puzzleDate() + "-ზე");
                return "redirect:/admin/connections";
            } catch (DuplicatePuzzleDateException e) {
                bindingResult.rejectValue(e.getField(), "duplicate", e.getMessage());
            } catch (IllegalArgumentException e) {
                bindingResult.reject("invalidGroups", e.getMessage());
            }
        }
        model.addAttribute("puzzles", adminConnectionsService.upcomingPuzzles());
        return "admin/connections";
    }

    // this is for the case where it pre fills thos e 4 groups that are empty
    private static CreateConnectionsPuzzleRequest emptyRequest() {
        List<ConnectionsGroupRequest> groups = new ArrayList<>();
        for (int i = 0; i < AdminConnectionsService.GROUPS_PER_PUZZLE; i++) {
            groups.add(new ConnectionsGroupRequest(null, 1, new ArrayList<>(Arrays.asList("", "", "", ""))));
        }
        return new CreateConnectionsPuzzleRequest(null, groups);
    }
}
