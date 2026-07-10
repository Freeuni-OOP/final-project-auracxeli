package com.auracxeli.connections;

import com.auracxeli.AbstractIntegrationTest;
import com.auracxeli.user.User;
import com.auracxeli.user.UserDetailsImpl;
import com.auracxeli.user.UserRepository;
import org.hamcrest.Matchers;
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

/**
 * Today's seeded puzzle (see V10__seed_connections_puzzles.sql, day-offset 2):
 * ტრანსპორტი(1): მანქანა/ავტობუსი/მატარებელი/თვითმფრინავი
 * რეპტილიები/ამფიბიები(2): გველი/ხვლიკი/ნიანგი/ქამელეონი
 * ბანქოს კარტები(3): ვალეტი/დამა/ტუზი/მეფე
 * ჭადრაკის ფიგურები(4): კუ/ლაზიერი/პაიკი/ეტლი
 */
@Transactional
class ConnectionsControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;

    private RequestPostProcessor asPlayerOne;

    @BeforeEach
    void setUp() {
        User savedUser = userRepository.save(new User("cplayer", "cplayer@example.com", "hash"));
        asPlayerOne = SecurityMockMvcRequestPostProcessors.user(new UserDetailsImpl(savedUser));
    }

    @Test
    void getConnections_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/connections"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void getConnections_authenticated_showsBoard() throws Exception {
        mockMvc.perform(get("/connections").with(asPlayerOne))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("connections-grid")));
    }

    @Test
    void postGuess_correctGroup_isAcceptedAndRedirects() throws Exception {
        mockMvc.perform(post("/connections/guess")
                        .with(asPlayerOne)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("words", "მანქანა", "ავტობუსი", "მატარებელი", "თვითმფრინავი"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/connections"));

        mockMvc.perform(get("/connections").with(asPlayerOne))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("ტრანსპორტი")));
    }

    @Test
    void postGuess_fourWrongGuesses_endsInLossAndRevealsGroups() throws Exception {
        String[][] wrongGuesses = {
                {"მანქანა", "გველი", "ვალეტი", "კუ"},
                {"ავტობუსი", "ხვლიკი", "დამა", "ლაზიერი"},
                {"მატარებელი", "ნიანგი", "ტუზი", "პაიკი"},
                {"თვითმფრინავი", "ქამელეონი", "მეფე", "ეტლი"}
        };
        for (String[] guess : wrongGuesses) {
            mockMvc.perform(post("/connections/guess")
                            .with(asPlayerOne)
                            .with(SecurityMockMvcRequestPostProcessors.csrf())
                            .param("words", guess))
                    .andExpect(status().is3xxRedirection());
        }

        mockMvc.perform(get("/connections").with(asPlayerOne))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("თამაში დამთავრდა")));
    }

    @Test
    void postGuess_wrongWordCount_showsErrorAndDoesNotConsumeAMistake() throws Exception {
        mockMvc.perform(post("/connections/guess")
                        .with(asPlayerOne)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("words", "მანქანა", "ავტობუსი"))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("არასწორი მონიშვნა")));
    }

    @Test
    void postGuess_afterGameWon_isRejectedAsAlreadyCompleted() throws Exception {
        String[][] allFourGroups = {
                {"მანქანა", "ავტობუსი", "მატარებელი", "თვითმფრინავი"},
                {"გველი", "ხვლიკი", "ნიანგი", "ქამელეონი"},
                {"ვალეტი", "დამა", "ტუზი", "მეფე"},
                {"კუ", "ლაზიერი", "პაიკი", "ეტლი"}
        };
        for (String[] guess : allFourGroups) {
            mockMvc.perform(post("/connections/guess")
                            .with(asPlayerOne)
                            .with(SecurityMockMvcRequestPostProcessors.csrf())
                            .param("words", guess))
                    .andExpect(status().is3xxRedirection());
        }

        mockMvc.perform(post("/connections/guess")
                        .with(asPlayerOne)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("words", "მანქანა", "ავტობუსი", "მატარებელი", "თვითმფრინავი"))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("არასწორი მონიშვნა")));
    }
}
