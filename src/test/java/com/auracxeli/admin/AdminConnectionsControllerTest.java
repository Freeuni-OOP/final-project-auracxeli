package com.auracxeli.admin;

import com.auracxeli.admin.dto.ScheduledGroup;
import com.auracxeli.admin.dto.ScheduledPuzzle;
import com.auracxeli.config.SecurityConfig;
import com.auracxeli.user.User;
import com.auracxeli.user.UserDetailsImpl;
import com.auracxeli.user.UserRepository;
import com.auracxeli.user.UserStatsService;
import com.auracxeli.user.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(AdminConnectionsController.class)
@Import(SecurityConfig.class)
class AdminConnectionsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminConnectionsService adminConnectionsService;

    @MockitoBean
    private UserStatsService userStatsService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private UserRepository userRepository;

    private UsernamePasswordAuthenticationToken adminAuth() {
        User admin = new User("admin", "admin@example.com", "hashed");
        admin.setRole(Role.ADMIN);
        UserDetailsImpl principal = new UserDetailsImpl(admin);
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getConnections_isAccessibleToAdmin_andShowsForm() throws Exception {
        when(adminConnectionsService.upcomingPuzzles()).thenReturn(List.of());

        mockMvc.perform(get("/admin/connections"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/connections"))
                .andExpect(model().attributeExists("createConnectionsPuzzleRequest", "puzzles"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getConnections_isForbiddenForNonAdmin() throws Exception {
        mockMvc.perform(get("/admin/connections"))
                .andExpect(status().isForbidden());
    }

    @Test
    void postConnections_validSubmit_savesAndRedirects() throws Exception {
        LocalDate date = LocalDate.now().plusDays(3);
        when(adminConnectionsService.createPuzzle(any())).thenReturn(
                new ScheduledPuzzle(date, List.of(
                        new ScheduledGroup("ხილი", 1),
                        new ScheduledGroup("ცხოველი", 2),
                        new ScheduledGroup("ფერი", 3),
                        new ScheduledGroup("ქალაქი", 4))));

        mockMvc.perform(post("/admin/connections")
                        .with(authentication(adminAuth()))
                        .with(csrf())
                        .params(validPuzzleParams(date)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/connections"));

        verify(adminConnectionsService).createPuzzle(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void postConnections_missingDate_showsValidationErrorAndDoesNotSave() throws Exception {
        when(adminConnectionsService.upcomingPuzzles()).thenReturn(List.of());
        org.springframework.util.LinkedMultiValueMap<String, String> params = validPuzzleParams(LocalDate.now().plusDays(1));
        params.remove("puzzleDate");

        mockMvc.perform(post("/admin/connections")
                        .with(csrf())
                        .params(params))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/connections"))
                .andExpect(model().attributeHasFieldErrors("createConnectionsPuzzleRequest", "puzzleDate"));

        verify(adminConnectionsService, never()).createPuzzle(any());
    }

    @Test
    void postConnections_duplicateDate_showsErrorOnDateField() throws Exception {
        when(adminConnectionsService.upcomingPuzzles()).thenReturn(List.of());
        LocalDate date = LocalDate.now().plusDays(3);
        doThrow(new DuplicatePuzzleDateException("puzzleDate", "ამ თარიღზე უკვე დაგეგმილია პაზლი"))
                .when(adminConnectionsService).createPuzzle(any());

        mockMvc.perform(post("/admin/connections")
                        .with(authentication(adminAuth()))
                        .with(csrf())
                        .params(validPuzzleParams(date)))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/connections"))
                .andExpect(model().attributeHasFieldErrors("createConnectionsPuzzleRequest", "puzzleDate"));
    }

    @Test
    void postConnections_invalidGroupShape_showsGlobalErrorAndDoesNotSave() throws Exception {
        when(adminConnectionsService.upcomingPuzzles()).thenReturn(List.of());
        LocalDate date = LocalDate.now().plusDays(3);
        doThrow(new IllegalArgumentException("თითოეულ ჯგუფს უნდა ჰქონდეს ზუსტად 4 სიტყვა"))
                .when(adminConnectionsService).createPuzzle(any());

        mockMvc.perform(post("/admin/connections")
                        .with(authentication(adminAuth()))
                        .with(csrf())
                        .params(validPuzzleParams(date)))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/connections"));
    }

    private org.springframework.util.LinkedMultiValueMap<String, String> validPuzzleParams(LocalDate date) {
        org.springframework.util.LinkedMultiValueMap<String, String> params = new org.springframework.util.LinkedMultiValueMap<>();
        params.add("puzzleDate", date.toString());
        String[] categories = {"ხილი", "ცხოველი", "ფერი", "ქალაქი"};
        for (int g = 0; g < 4; g++) {
            params.add("groups[" + g + "].category", categories[g]);
            params.add("groups[" + g + "].difficulty", String.valueOf(g + 1));
            for (int w = 0; w < 4; w++) {
                params.add("groups[" + g + "].words[" + w + "]", "სიტყვა" + g + w);
            }
        }
        return params;
    }
}
