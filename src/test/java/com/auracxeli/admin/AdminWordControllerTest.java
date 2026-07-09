package com.auracxeli.admin;

import com.auracxeli.config.SecurityConfig;
import com.auracxeli.user.Role;
import com.auracxeli.user.User;
import com.auracxeli.user.UserDetailsImpl;
import com.auracxeli.user.UserRepository;
import com.auracxeli.wordle.InvalidGeorgianWordException;
import com.auracxeli.wordle.WordleWord;
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
import java.time.ZoneOffset;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

@WebMvcTest(AdminWordController.class)
@Import(SecurityConfig.class)
class AdminWordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminWordService adminWordService;

    @MockitoBean
    private UserDetailsService userDetailsService; // required by SecurityConfig's constructor

    @MockitoBean
    private UserRepository userRepository; // required by the global theme @ControllerAdvice

    private UsernamePasswordAuthenticationToken adminAuth() {
        User admin = new User("admin", "admin@example.com", "hashed");
        admin.setRole(Role.ADMIN);
        UserDetailsImpl principal = new UserDetailsImpl(admin);
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getWords_isAccessibleToAdmin_andShowsForm() throws Exception {
        when(adminWordService.upcomingWords()).thenReturn(List.of());

        mockMvc.perform(get("/admin/words"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/words"))
                .andExpect(model().attributeExists("addWordRequest", "words"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getWords_isForbiddenForNonAdmin() throws Exception {
        mockMvc.perform(get("/admin/words"))
                .andExpect(status().isForbidden());
    }

    @Test
    void postWord_validSubmit_savesAndRedirects() throws Exception {
        when(adminWordService.addWord(eq("ბურთი"), any(), any()))
                .thenReturn(new WordleWord("ბურთი", LocalDate.now(ZoneOffset.UTC), null));

        mockMvc.perform(post("/admin/words")
                        .with(authentication(adminAuth()))
                        .with(csrf())
                        .param("word", "ბურთი"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/words"));

        verify(adminWordService).addWord(eq("ბურთი"), any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void postWord_invalidSubmit_showsErrorsAndDoesNotSave() throws Exception {
        when(adminWordService.upcomingWords()).thenReturn(List.of());

        mockMvc.perform(post("/admin/words")
                        .with(csrf())
                        .param("word", "abcde")) // Latin letters -> fails the Georgian pattern
                .andExpect(status().isOk())
                .andExpect(view().name("admin/words"))
                .andExpect(model().attributeHasFieldErrors("addWordRequest", "word"));

        verify(adminWordService, never()).addWord(any(), any(), any());
    }

    @Test
    void postWord_uniquenessConflict_showsErrorOnWordField() throws Exception {
        when(adminWordService.upcomingWords()).thenReturn(List.of());
        doThrow(new DuplicateWordException("word", "ეს სიტყვა გამოყენებულია ბოლო 60 დღეში"))
                .when(adminWordService).addWord(eq("ბურთი"), any(), any());

        mockMvc.perform(post("/admin/words")
                        .with(authentication(adminAuth()))
                        .with(csrf())
                        .param("word", "ბურთი"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/words"))
                .andExpect(model().attributeHasFieldErrors("addWordRequest", "word"));
    }

    @Test
    void postWord_wordNotInDictionary_showsErrorOnWordField() throws Exception {
        when(adminWordService.upcomingWords()).thenReturn(List.of());
        doThrow(new InvalidGeorgianWordException())
                .when(adminWordService).addWord(eq("ააააა"), any(), any());

        mockMvc.perform(post("/admin/words")
                        .with(authentication(adminAuth()))
                        .with(csrf())
                        .param("word", "ააააა"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/words"))
                .andExpect(model().attributeHasFieldErrors("addWordRequest", "word"));
    }
}
