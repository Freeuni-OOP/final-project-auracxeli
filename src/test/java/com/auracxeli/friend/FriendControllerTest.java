package com.auracxeli.friend;

import com.auracxeli.config.SecurityConfig;
import com.auracxeli.friend.dto.FriendDto;
import com.auracxeli.friend.dto.PendingRequestDto;
import com.auracxeli.user.User;
import com.auracxeli.user.UserDetailsImpl;
import com.auracxeli.user.UserRepository;
import com.auracxeli.user.UserStatsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(FriendController.class)
@Import(SecurityConfig.class)
class FriendControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FriendService friendService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private UserStatsService userStatsService;

    private UsernamePasswordAuthenticationToken auth() {
        User user = new User("me", "me@example.com", "hash");
        user.setId(1L);
        UserDetailsImpl principal = new UserDetailsImpl(user);
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    @Test
    void getFriends_showsFriendsAndPendingRequests() throws Exception {
        when(friendService.listFriends(1L)).thenReturn(List.of(new FriendDto(1L, "buddy")));
        when(friendService.listPendingRequests(1L)).thenReturn(List.of(new PendingRequestDto(2L, "requester")));

        mockMvc.perform(get("/friends").with(authentication(auth())))
                .andExpect(status().isOk())
                .andExpect(view().name("friends"))
                .andExpect(model().attributeExists("friends", "pendingRequests"));
    }

    @Test
    void postRequest_valid_sendsRequestAndRedirects() throws Exception {
        mockMvc.perform(post("/friends/request")
                        .with(authentication(auth()))
                        .with(csrf())
                        .param("username", "someone"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/friends"));

        verify(friendService).sendRequest(1L, "someone");
    }

    @Test
    void postRequest_friendshipException_flashesErrorAndRedirects() throws Exception {
        doThrow(new FriendshipException("მომხმარებელი ვერ მოიძებნა: ghost"))
                .when(friendService).sendRequest(1L, "ghost");

        mockMvc.perform(post("/friends/request")
                        .with(authentication(auth()))
                        .with(csrf())
                        .param("username", "ghost"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/friends"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    void postAccept_valid_acceptsAndRedirects() throws Exception {
        mockMvc.perform(post("/friends/{id}/accept", 5L)
                        .with(authentication(auth()))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/friends"));

        verify(friendService).acceptRequest(1L, 5L);
    }

    @Test
    void postDecline_valid_declinesAndRedirects() throws Exception {
        mockMvc.perform(post("/friends/{id}/decline", 5L)
                        .with(authentication(auth()))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/friends"));

        verify(friendService).declineRequest(1L, 5L);
    }

    @Test
    void postRemove_valid_removesAndRedirects() throws Exception {
        mockMvc.perform(post("/friends/{id}/remove", 5L)
                        .with(authentication(auth()))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/friends"));

        verify(friendService).removeFriend(1L, 5L);
    }
}