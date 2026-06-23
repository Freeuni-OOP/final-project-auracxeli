package com.auracxeli.admin;

import com.auracxeli.admin.dto.AddWordRequest;
import com.auracxeli.user.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/words")
public class AdminWordController {

    private final AdminWordService adminWordService;

    public AdminWordController(AdminWordService adminWordService) {
        this.adminWordService = adminWordService;
    }

    @GetMapping
    public String showWords(Model model) {
        model.addAttribute("addWordRequest", new AddWordRequest(null, null));
        model.addAttribute("words", adminWordService.listWords());
        return "admin/words";
    }

    @PostMapping
    public String addWord(@Valid @ModelAttribute AddWordRequest addWordRequest,
                          BindingResult bindingResult,
                          @AuthenticationPrincipal UserDetailsImpl admin,
                          Model model) {
        if (!bindingResult.hasErrors()) {
            try {
                adminWordService.addWord(addWordRequest.word(), addWordRequest.scheduledDate(), admin.getId());
                return "redirect:/admin/words";
            } catch (DuplicateWordException e) {
                bindingResult.rejectValue(e.getField(), "duplicate", e.getMessage());
            }
        }
        model.addAttribute("words", adminWordService.listWords());
        return "admin/words";
    }
}
