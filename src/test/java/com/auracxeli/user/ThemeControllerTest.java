package com.auracxeli.user;

import com.auracxeli.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class ThemeControllerTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;

    @Test
    void toggle_authenticated_persistsNewThemeAndRedirectsBack() throws Exception {
        User saved = userRepository.save(new User("themer", "themer@example.com", "hash"));
        assertThat(saved.getThemePreference()).isEqualTo(Theme.LIGHT); // new users default LIGHT

        mockMvc.perform(post("/theme/toggle")
                        .with(user(new UserDetailsImpl(saved)))
                        .with(csrf())
                        .header("Referer", "http://localhost/profile"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"));

        assertThat(userRepository.findById(saved.getId()).orElseThrow().getThemePreference())
                .isEqualTo(Theme.DARK);
    }

    @Test
    void toggle_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(post("/theme/toggle").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }
}
