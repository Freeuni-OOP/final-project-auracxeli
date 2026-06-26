package com.auracxeli.user;

import com.auracxeli.user.dto.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import lombok.RequiredArgsConstructor;

import org.springframework.validation.BindingResult;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class RegisterController {
    private final UserService userService;


    @GetMapping("/register")
    public String showForm(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest(null, null, null));
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute RegisterRequest registerRequest,
                           BindingResult bindingResult) {
        //ვალიდაციას თუ ვერ გაივლის თავიდან ცდის.
        if (bindingResult.hasErrors()) {
            return "register";
        }
        try {
            userService.register(registerRequest);
        } catch (DuplicateUserException e) {
            //username an emaili dakavebuli tua
            bindingResult.rejectValue(e.getField(), "duplicate", e.getMessage());
            return "register";
        }

        return "redirect:/login";
    }
}