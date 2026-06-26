package com.auracxeli.user;

import com.auracxeli.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@Transactional
class ProfileControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;

    private RequestPostProcessor asViewer;

    @BeforeEach
    void setUp() {
        User viewer = userRepository.save(new User("viewer", "viewer@example.com", "hash"));
        userRepository.save(new User("someoneelse", "someoneelse@example.com", "hash"));
        asViewer = SecurityMockMvcRequestPostProcessors.user(new UserDetailsImpl(viewer));
    }

    @Test
    void getOwnProfile_authenticated_showsProfile() throws Exception {
        mockMvc.perform(get("/profile").with(asViewer))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(content().string(containsString("viewer")));
    }

    @Test
    void getNamedProfile_existingUser_showsThatUser() throws Exception {
        mockMvc.perform(get("/profile/someoneelse").with(asViewer))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(content().string(containsString("someoneelse")));
    }

    @Test
    void getNamedProfile_unknownUser_returnsNotFound() throws Exception {
        mockMvc.perform(get("/profile/ghost").with(asViewer))
                .andExpect(status().isNotFound());
    }

    @Test
    void getProfile_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/profile"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }
}
