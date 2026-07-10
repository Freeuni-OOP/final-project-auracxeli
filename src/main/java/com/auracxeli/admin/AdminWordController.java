package com.auracxeli.admin;

import com.auracxeli.admin.dto.AddWordRequest;
import com.auracxeli.admin.dto.ScheduledWord;
import com.auracxeli.user.UserDetailsImpl;
import com.auracxeli.wordle.InvalidGeorgianWordException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("/admin/words")
@RequiredArgsConstructor
public class AdminWordController {

    private final AdminWordService adminWordService;

    @GetMapping
    public String showWords(Model model) {
        model.addAttribute("addWordRequest", new AddWordRequest(null, null));
        model.addAttribute("words", adminWordService.upcomingWords());
        return "admin/words";
    }

    @PostMapping
    public String addWord(@Valid @ModelAttribute AddWordRequest addWordRequest,
                          BindingResult bindingResult,
                          @AuthenticationPrincipal UserDetailsImpl admin,
                          RedirectAttributes redirectAttributes,
                          Model model) {
        if (bindingResult.hasErrors()) {
            log.warn("Word submission rejected: {} validation error(s)", bindingResult.getErrorCount());
        } else {
            try {
                ScheduledWord saved = adminWordService.addWord(
                        addWordRequest.word(), addWordRequest.scheduledDate(), admin.getId());
                redirectAttributes.addFlashAttribute("message",
                        "სიტყვა დაემატა " + saved.scheduledDate() + "-ზე");
                return "redirect:/admin/words";
            } catch (InvalidGeorgianWordException e) {
                bindingResult.rejectValue("word", "invalidWord", e.getMessage());
            } catch (DuplicateWordException e) {
                // AdminWordService already logs the rejection (with the business reason); just surface it.
                bindingResult.rejectValue(e.getField(), "duplicate", e.getMessage());
            }
        }
        model.addAttribute("words", adminWordService.upcomingWords());
        return "admin/words";
    }
}
