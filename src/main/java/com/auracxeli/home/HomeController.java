package com.auracxeli.home;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serves the landing page. This exists (instead of relying on Spring Boot's
 * welcome-page handler) so that @ControllerAdvice model attributes - notably the
 * user's theme - are applied to the home page too.
 */
@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "index";
    }
}
