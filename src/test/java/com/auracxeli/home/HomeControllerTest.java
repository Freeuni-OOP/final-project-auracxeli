package com.auracxeli.home;

import com.auracxeli.AbstractIntegrationTest;
import com.auracxeli.user.Theme;
import com.auracxeli.user.User;
import com.auracxeli.user.UserDetailsImpl;
import com.auracxeli.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@Transactional
class HomeControllerTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;

    @Test
    void home_appliesLoggedInUsersStoredTheme() throws Exception {
        User darkUser = new User("darklover", "dark@example.com", "hash");
        darkUser.setThemePreference(Theme.DARK);
        userRepository.save(darkUser);

        mockMvc.perform(get("/").with(user(new UserDetailsImpl(darkUser))))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(content().string(containsString("data-theme=\"dark\"")));
    }

    @Test
    void home_anonymous_defaultsToLight() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("data-theme=\"light\"")));
    }
}
