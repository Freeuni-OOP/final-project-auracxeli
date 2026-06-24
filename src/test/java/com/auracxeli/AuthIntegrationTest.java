package com.auracxeli;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@AutoConfigureMockMvc
public class AuthIntegrationTest extends AbstractIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    private static final String REGISTER_URL = "/register";
    private static final String LOGIN_URL = "/login";
    private static final String LOGOUT_URL = "/logout";
    private static final String PROFILE_URL = "/profile";

    private static final String USERNAME = "USerTest_" + System.currentTimeMillis();
    private static final String EMAIL = "UserEmailTest_" + System.currentTimeMillis() + "@test.com";
    private static final String PASSWORD = "Passw0rd>";

    @Test
    public void successfulRegistrationLoginAndProfileAccess() throws Exception {
        // Step 1 - register
        mockMvc.perform(post(REGISTER_URL).with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", USERNAME)
                        .param("email", EMAIL)
                        .param("password", PASSWORD))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        // Step 2 - login
        final var loginResult = mockMvc.perform(post(LOGIN_URL).with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", USERNAME)
                        .param("password", PASSWORD))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andReturn();
        // Step 3 - access profile
        final var session = (MockHttpSession) loginResult.getRequest().getSession(false);
        mockMvc.perform(get(PROFILE_URL).session(session))
                .andExpect(status().isOk());
    }
    @Test
    public void loginWithErrPassword() throws Exception {
        final String existingUser = "existing_" + System.currentTimeMillis();
        mockMvc.perform(post(REGISTER_URL).with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", existingUser)
                .param("email", "exists_" + System.currentTimeMillis() + "@test.com")
                .param("password", PASSWORD));

        mockMvc.perform(post(LOGIN_URL).with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", existingUser)
                        .param("password", "WrongPassword!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    public void registerWithDuplicateUsername_returnsValidationError() throws Exception {
        final String duplicateUser = "dup_" + System.currentTimeMillis();
        final String duplicatePass = "PassCode2";
        // First registration - should succeed with redirect
        mockMvc.perform(post(REGISTER_URL).with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", duplicateUser)
                        .param("email", "duplicate1_" + System.currentTimeMillis() + "@test.com")
                        .param("password", duplicatePass))
                .andExpect(status().is3xxRedirection());

        // Second registration - should fail with 200 (form shown again with error)
        mockMvc.perform(post(REGISTER_URL).with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", duplicateUser)
                        .param("email", "duplicate2_" + System.currentTimeMillis() + "@test.com")
                        .param("password", duplicatePass))
                .andExpect(status().isOk());
    }

    @Test
    @org.junit.jupiter.api.Disabled("Profile page not yet implemented - waiting for teammate")
    public void afterLogout_protectedPageRedirectsToLogin() throws Exception {
        final String logoutUser = "logout_" + System.currentTimeMillis();

        // Register
        mockMvc.perform(post(REGISTER_URL).with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", logoutUser)
                        .param("email", "logout_" + System.currentTimeMillis() + "@test.com")
                        .param("password", PASSWORD))
                .andExpect(status().is3xxRedirection());

        // Login
        final var loginResult = mockMvc.perform(post(LOGIN_URL).with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", logoutUser)
                        .param("password", PASSWORD))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        final var session = (MockHttpSession) loginResult.getRequest().getSession(false);

        // Profile works before logout
        mockMvc.perform(get(PROFILE_URL).session(session))
                .andExpect(status().isOk());

        // Logout
        mockMvc.perform(post(LOGOUT_URL).with(csrf()).session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        // After logout, profile redirects to login
        MockHttpSession newSession = new MockHttpSession();
        mockMvc.perform(get(PROFILE_URL).session(newSession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login**"));
    }
}
