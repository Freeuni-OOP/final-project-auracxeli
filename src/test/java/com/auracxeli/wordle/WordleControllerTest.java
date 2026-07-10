package com.auracxeli.wordle;

import com.auracxeli.AbstractIntegrationTest;
import com.auracxeli.user.User;
import com.auracxeli.user.UserDetailsImpl;
import com.auracxeli.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
class WordleControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;

    private static final String TARGET_WORD = "წიგნი";
    private RequestPostProcessor asPlayerOne;

    @BeforeEach
    void setUp() {
        User savedUser = userRepository.save(new User("playerone", "playerone@example.com", "hash"));
        asPlayerOne = SecurityMockMvcRequestPostProcessors.user(new UserDetailsImpl(savedUser));
    }

    @Test
    void getWordle_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/wordle"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void getWordle_authenticated_showsBoard() throws Exception {
        mockMvc.perform(get("/wordle").with(asPlayerOne))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("wordle-board")));
    }

    @Test
    void postGuess_correctWord_endsInWin() throws Exception {
        mockMvc.perform(post("/wordle/guess")
                        .with(asPlayerOne)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("guess", TARGET_WORD))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/wordle"));

        mockMvc.perform(get("/wordle").with(asPlayerOne))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("გილოცავ")));
    }

    @Test
    void postGuess_sixWrongGuesses_endsInLossAndRevealsWord() throws Exception {
        for (int i = 0; i < 6; i++) {
            mockMvc.perform(post("/wordle/guess")
                            .with(asPlayerOne)
                            .with(SecurityMockMvcRequestPostProcessors.csrf())
                            .param("guess", "ბარგი"))
                    .andExpect(status().is3xxRedirection());
        }

        mockMvc.perform(get("/wordle").with(asPlayerOne))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(TARGET_WORD)));
    }

    @Test
    void postGuess_invalidGeorgianWord_showsErrorAndDoesNotConsumeAttempt() throws Exception {
        // "ააააა" is five real Georgian letters but not a dictionary word.
        mockMvc.perform(post("/wordle/guess")
                        .with(asPlayerOne)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("guess", "ააააა"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        org.hamcrest.Matchers.containsString(InvalidGeorgianWordException.MESSAGE)));

        // Attempt was rejected before evaluation, so the board is still empty (0 used).
        mockMvc.perform(get("/wordle").with(asPlayerOne))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"guess\"")));
    }

    @Test
    void postGuess_validDictionaryWord_isAcceptedAndRedirects() throws Exception {
        // "ბარგი" is a valid dictionary word but not today's answer.
        mockMvc.perform(post("/wordle/guess")
                        .with(asPlayerOne)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("guess", "ბარგი"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/wordle"));
    }

    @Test
    void postGuess_emptyGuess_doesNotErrorAndShowsBoard() throws Exception {
        mockMvc.perform(post("/wordle/guess")
                        .with(asPlayerOne)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("guess", ""))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("wordle-board")));
    }

    @Test
    void getWordle_afterCompletion_showsReadOnlyBoard() throws Exception {
        mockMvc.perform(post("/wordle/guess")
                .with(asPlayerOne)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .param("guess", TARGET_WORD));

        mockMvc.perform(get("/wordle").with(asPlayerOne))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("name=\"guess\""))));
    }
}
