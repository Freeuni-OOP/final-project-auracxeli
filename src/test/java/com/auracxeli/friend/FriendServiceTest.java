package com.auracxeli.friend;

import com.auracxeli.friend.dto.FriendDto;
import com.auracxeli.friend.dto.PendingRequestDto;
import com.auracxeli.user.User;
import com.auracxeli.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FriendServiceTest {

    @Mock private FriendshipRepository friendshipRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private FriendService friendService;

    private User user(Long id, String username) {
        User u = mock(User.class);
        lenient().when(u.getId()).thenReturn(id);
        lenient().when(u.getUsername()).thenReturn(username);
        return u;
    }

    @Test
    void sendRequestSavesPendingRequestTest() {
        User addressee = user(2L, "nika");
        User requester = user(1L, "zuka");
        when(userRepository.findByUsername("nika")).thenReturn(Optional.of(addressee));
        when(friendshipRepository.findBetween(1L, 2L)).thenReturn(Optional.empty());
        when(userRepository.getReferenceById(1L)).thenReturn(requester);

        friendService.sendRequest(1L, "nika");

        verify(friendshipRepository).save(any(Friendship.class));
    }

    @Test
    void sendRequestToUnknownUserThrowsTest() {
        when(userRepository.findByUsername("aravin")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> friendService.sendRequest(1L, "aravin"))
                .isInstanceOf(FriendshipException.class);
        verify(friendshipRepository, never()).save(any());
    }

    @Test
    void sendRequestToSelfThrowsTest() {
        User self = user(1L, "zuka");
        when(userRepository.findByUsername("zuka")).thenReturn(Optional.of(self));

        assertThatThrownBy(() -> friendService.sendRequest(1L, "zuka"))
                .isInstanceOf(FriendshipException.class);
        verify(friendshipRepository, never()).save(any());
    }

    @Test
    void sendRequestWhenAlreadyExistsThrowsTest() {
        User addressee = user(2L, "nika");
        when(userRepository.findByUsername("nika")).thenReturn(Optional.of(addressee));
        when(friendshipRepository.findBetween(1L, 2L)).thenReturn(Optional.of(mock(Friendship.class)));

        assertThatThrownBy(() -> friendService.sendRequest(1L, "nika"))
                .isInstanceOf(FriendshipException.class);
        verify(friendshipRepository, never()).save(any());
    }

    @Test
    void acceptRequestSetsStatusAcceptedTest() {
        User zuka = user(1L, "zuka");
        User nika = user(2L, "nika");
        Friendship friendship = new Friendship(nika, zuka);
        when(friendshipRepository.findById(10L)).thenReturn(Optional.of(friendship));

        friendService.acceptRequest(1L, 10L);

        assertThat(friendship.getStatus()).isEqualTo(FriendshipStatus.ACCEPTED);
        verify(friendshipRepository).save(friendship);
    }

    @Test
    void acceptRequestByNonAddresseeThrowsTest() {
        User zuka = user(1L, "zuka");
        User nika = user(2L, "nika");
        Friendship friendship = new Friendship(nika, zuka);
        when(friendshipRepository.findById(10L)).thenReturn(Optional.of(friendship));

        assertThatThrownBy(() -> friendService.acceptRequest(99L, 10L))
                .isInstanceOf(FriendshipException.class);
        verify(friendshipRepository, never()).save(any());
    }

    @Test
    void declineRequestDeletesItTest() {
        User zuka = user(1L, "zuka");
        User nika = user(2L, "nika");
        Friendship friendship = new Friendship(nika, zuka);
        when(friendshipRepository.findById(10L)).thenReturn(Optional.of(friendship));

        friendService.declineRequest(1L, 10L);

        verify(friendshipRepository).delete(friendship);
    }

    @Test
    void removeFriendDeletesItTest() {
        User zuka = user(1L, "zuka");
        User nika = user(2L, "nika");
        Friendship friendship = new Friendship(zuka, nika);
        friendship.setStatus(FriendshipStatus.ACCEPTED);
        when(friendshipRepository.findById(10L)).thenReturn(Optional.of(friendship));

        friendService.removeFriend(1L, 10L);

        verify(friendshipRepository).delete(friendship);
    }

    @Test
    void removeFriendNotInvolvedThrowsTest() {
        User zuka = user(1L, "zuka");
        User nika = user(2L, "nika");
        Friendship friendship = new Friendship(zuka, nika);
        when(friendshipRepository.findById(10L)).thenReturn(Optional.of(friendship));

        assertThatThrownBy(() -> friendService.removeFriend(99L, 10L))
                .isInstanceOf(FriendshipException.class);
        verify(friendshipRepository, never()).delete(any());
    }

    @Test
    void listFriendsReturnsOtherPartyTest() {
        User zuka = user(1L, "zuka");
        User nika = user(2L, "nika");
        Friendship friendship = new Friendship(nika, zuka);
        friendship.setStatus(FriendshipStatus.ACCEPTED);
        when(friendshipRepository.findByStatusForUser(1L, FriendshipStatus.ACCEPTED))
                .thenReturn(List.of(friendship));

        List<FriendDto> friends = friendService.listFriends(1L);

        assertThat(friends).hasSize(1);
        assertThat(friends.get(0).username()).isEqualTo("nika");
    }

    @Test
    void listPendingRequestsReturnsIncomingTest() {
        User zuka = user(1L, "zuka");
        User luka = user(3L, "luka");
        Friendship request = new Friendship(luka, zuka);
        when(friendshipRepository.findByAddresseeIdAndStatus(1L, FriendshipStatus.PENDING))
                .thenReturn(List.of(request));

        List<PendingRequestDto> pending = friendService.listPendingRequests(1L);

        assertThat(pending).hasSize(1);
        assertThat(pending.get(0).requesterUsername()).isEqualTo("luka");
    }

    @Test
    void sendRequestWhenAlreadyFriendsThrowsTest() {
        User addressee = user(2L, "nika");
        Friendship accepted = mock(Friendship.class);
        when(accepted.getStatus()).thenReturn(FriendshipStatus.ACCEPTED);
        when(userRepository.findByUsername("nika")).thenReturn(Optional.of(addressee));
        when(friendshipRepository.findBetween(1L, 2L)).thenReturn(Optional.of(accepted));

        assertThatThrownBy(() -> friendService.sendRequest(1L, "nika"))
                .isInstanceOf(FriendshipException.class)
                .hasMessage("თქვენ უკვე მეგობრები ხართ");
        verify(friendshipRepository, never()).save(any());
    }

    @Test
    void acceptRequestWhenNotFoundThrowsTest() {
        when(friendshipRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> friendService.acceptRequest(1L, 10L))
                .isInstanceOf(FriendshipException.class);
        verify(friendshipRepository, never()).save(any());
    }

    @Test
    void acceptRequestWhenNotPendingThrowsTest() {
        User zuka = user(1L, "zuka");
        User nika = user(2L, "nika");
        Friendship friendship = new Friendship(nika, zuka);
        friendship.setStatus(FriendshipStatus.ACCEPTED);
        when(friendshipRepository.findById(10L)).thenReturn(Optional.of(friendship));

        assertThatThrownBy(() -> friendService.acceptRequest(1L, 10L))
                .isInstanceOf(FriendshipException.class);
        verify(friendshipRepository, never()).save(any());
    }

    @Test
    void removeFriendWhenNotFoundThrowsTest() {
        when(friendshipRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> friendService.removeFriend(1L, 10L))
                .isInstanceOf(FriendshipException.class);
        verify(friendshipRepository, never()).delete(any());
    }

    @Test
    void removeFriendByAddresseeDeletesItTest() {
        User zuka = user(1L, "zuka");
        User nika = user(2L, "nika");
        Friendship friendship = new Friendship(nika, zuka);
        friendship.setStatus(FriendshipStatus.ACCEPTED);
        when(friendshipRepository.findById(10L)).thenReturn(Optional.of(friendship));

        friendService.removeFriend(1L, 10L);

        verify(friendshipRepository).delete(friendship);
    }

    @Test
    void listFriendsReturnsAddresseeWhenUserIsRequesterTest() {
        User zuka = user(1L, "zuka");
        User nika = user(2L, "nika");
        Friendship friendship = new Friendship(zuka, nika);
        friendship.setStatus(FriendshipStatus.ACCEPTED);
        when(friendshipRepository.findByStatusForUser(1L, FriendshipStatus.ACCEPTED)).thenReturn(List.of(friendship));

        List<FriendDto> friends = friendService.listFriends(1L);

        assertThat(friends).hasSize(1);
        assertThat(friends.get(0).username()).isEqualTo("nika");
    }
}
