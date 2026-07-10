package com.auracxeli.admin;

import com.auracxeli.config.SecurityConfig;
import com.auracxeli.user.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(AdminUserController.class)
@Import(SecurityConfig.class)
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminUserService adminUserService;

    @MockitoBean
    private UserStatsService userStatsService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private UserRepository userRepository; // required by the global theme @ControllerAdvice

    @Test
    void getUsersAdminShowsUsersTest() throws Exception {
        when(adminUserService.listUsers("")).thenReturn(List.of(
                user(1L, "admin", Role.ADMIN, true),
                user(2L, "gio", Role.USER, true)
        ));

        mockMvc.perform(get("/admin/users").with(authentication(adminAuth())))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/users"))
                .andExpect(model().attributeExists("users", "currentAdminId"))
                .andExpect(content().string(containsString("gio@example.com")));
    }

    @Test
    void getUsersWithQuerySearchesUsersTest() throws Exception {
        when(adminUserService.listUsers("gio")).thenReturn(List.of(
                user(2L, "gio", Role.USER, true)
        ));

        mockMvc.perform(get("/admin/users")
                        .param("q", "gio")
                        .with(authentication(adminAuth())))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/users"))
                .andExpect(model().attribute("q", "gio"))
                .andExpect(content().string(containsString("gio@example.com")));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getUsersNonAdminIsForbiddenTest() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    void toggleActiveValidTargetCallsServiceAndRedirectsTest() throws Exception {
        User target = user(2L, "gio", Role.USER, false);
        when(adminUserService.toggleActive(2L, 1L)).thenReturn(target);

        mockMvc.perform(post("/admin/users/2/toggle-active")
                        .with(authentication(adminAuth()))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"))
                .andExpect(flash().attribute("message", "მომხმარებელი დაიბლოკა"));

        verify(adminUserService).toggleActive(2L, 1L);
    }

    @Test
    void toggleActiveKeepsSearchQueryTest() throws Exception {
        User target = user(2L, "gio", Role.USER, true);
        when(adminUserService.toggleActive(2L, 1L)).thenReturn(target);

        mockMvc.perform(post("/admin/users/2/toggle-active")
                        .param("q", "gio")
                        .with(authentication(adminAuth()))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users?q=gio"));

        verify(adminUserService).toggleActive(2L, 1L);
    }

    @Test
    void deleteUserValidTargetCallsServiceAndRedirectsTest() throws Exception {
        mockMvc.perform(post("/admin/users/2/delete")
                        .with(authentication(adminAuth()))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"))
                .andExpect(flash().attribute("message", "მომხმარებელი წაიშალა"));

        verify(adminUserService).deleteUser(2L, 1L);
    }

    @Test
    void toggleActiveDeniedTargetRedirectsWithErrorTest() throws Exception {
        doThrow(new AdminUserActionDeniedException("ადმინის ანგარიშის შეცვლა აკრძალულია"))
                .when(adminUserService).toggleActive(2L, 1L);

        mockMvc.perform(post("/admin/users/2/toggle-active")
                        .with(authentication(adminAuth()))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"))
                .andExpect(flash().attribute("error", "ადმინის ანგარიშის შეცვლა აკრძალულია"));
    }

    private UsernamePasswordAuthenticationToken adminAuth() {
        User admin = user(1L, "admin", Role.ADMIN, true);
        UserDetailsImpl principal = new UserDetailsImpl(admin);
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    private User user(Long id, String username, Role role, boolean active) {
        User user = new User(username, username + "@example.com", "password");
        user.setId(id);
        user.setRole(role);
        user.setActive(active);
        return user;
    }
}
