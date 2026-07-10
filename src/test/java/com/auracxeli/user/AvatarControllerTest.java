package com.auracxeli.user;

import com.auracxeli.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

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

@WebMvcTest(AvatarController.class)
@Import(SecurityConfig.class)
class AvatarControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AvatarService avatarService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private UserStatsService userStatsService;

    private User user(Long id, String avatar) {
        User user = new User("tester", "tester@example.com", "hash");
        user.setId(id);
        user.setAvatar(avatar);
        return user;
    }

    private UsernamePasswordAuthenticationToken auth(User user) {
        UserDetailsImpl principal = new UserDetailsImpl(user);
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    @Test
    void getAvatarPicker_showsAvailableAvatarsAndCurrentChoice() throws Exception {
        User currentUser = user(1L, "avatar-2.png");
        when(avatarService.availableAvatars()).thenReturn(List.of("avatar-1.png", "avatar-2.png"));

        mockMvc.perform(get("/profile/avatar").with(authentication(auth(currentUser))))
                .andExpect(status().isOk())
                .andExpect(view().name("avatar-picker"))
                .andExpect(model().attribute("currentAvatar", "avatar-2.png"))
                .andExpect(model().attribute("avatars", List.of("avatar-1.png", "avatar-2.png")));
    }

    @Test
    void postAvatarPicker_validChoice_savesAndRedirectsToProfile() throws Exception {
        User currentUser = user(1L, "avatar-2.png");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L, "avatar-5.png")));

        mockMvc.perform(post("/profile/avatar")
                        .with(authentication(auth(currentUser)))
                        .with(csrf())
                        .param("avatar", "avatar-5.png"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"));

        verify(avatarService).chooseAvatar(1L, "avatar-5.png");
    }
}