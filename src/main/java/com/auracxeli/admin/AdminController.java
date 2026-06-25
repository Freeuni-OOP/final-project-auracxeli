package com.auracxeli.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Admin landing page. Access is restricted to ROLE_ADMIN by the
 * {@code /admin/**} rule in SecurityConfig.
 */
@Controller
public class AdminController {

    @GetMapping("/admin")
    public String dashboard() {
        return "admin/dashboard";
    }
}
