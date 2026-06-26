package com.auracxeli.friend;

import com.auracxeli.friend.dto.FriendDto;
import com.auracxeli.friend.dto.PendingRequestDto;
import com.auracxeli.user.User;
import com.auracxeli.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FriendService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    public FriendService(FriendshipRepository friendshipRepository, UserRepository userRepository) {
        this.friendshipRepository = friendshipRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void sendRequest(Long requesterId, String addresseeUsername) {
        User addressee = userRepository.findByUsername(addresseeUsername)
                .orElseThrow(() -> new FriendshipException("მომხმარებელი ვერ მოიძებნა: " + addresseeUsername));

        if (addressee.getId().equals(requesterId)) {
            throw new FriendshipException("საკუთარი თავის დამეგობრება შეუძლებელია");
        }
        friendshipRepository.findBetween(requesterId, addressee.getId()).ifPresent(existing -> {
            if (existing.getStatus() == FriendshipStatus.ACCEPTED) {
                throw new FriendshipException("თქვენ უკვე მეგობრები ხართ");
            }
            throw new FriendshipException("მეგობრობის მოთხოვნა უკვე არსებობს");
        });

        User requester = userRepository.getReferenceById(requesterId);
        friendshipRepository.save(new Friendship(requester, addressee));
    }

    @Transactional
    public void acceptRequest(Long currentUserId, Long friendshipId) {
        Friendship friendship = pendingRequestFor(currentUserId, friendshipId);
        friendship.setStatus(FriendshipStatus.ACCEPTED);
        friendshipRepository.save(friendship);
    }

    @Transactional
    public void declineRequest(Long currentUserId, Long friendshipId) {
        Friendship friendship = pendingRequestFor(currentUserId, friendshipId);
        friendshipRepository.delete(friendship);
    }

    @Transactional
    public void removeFriend(Long currentUserId, Long friendshipId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new FriendshipException("მეგობრობა ვერ მოიძებნა"));
        if (!involves(friendship, currentUserId)) {
            throw new FriendshipException("ეს მეგობრობა თქვენი არ არის");
        }
        friendshipRepository.delete(friendship);
    }

    @Transactional(readOnly = true)
    public List<FriendDto> listFriends(Long userId) {
        return friendshipRepository.findByStatusForUser(userId, FriendshipStatus.ACCEPTED).stream()
                .map(friendship -> new FriendDto(friendship.getId(), otherParty(friendship, userId).getUsername()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PendingRequestDto> listPendingRequests(Long userId) {
        return friendshipRepository.findByAddresseeIdAndStatus(userId, FriendshipStatus.PENDING).stream()
                .map(friendship -> new PendingRequestDto(friendship.getId(), friendship.getRequester().getUsername()))
                .toList();
    }

    // only adresse can accept or reject pending request
    private Friendship pendingRequestFor(Long currentUserId, Long friendshipId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new FriendshipException("მოთხოვნა ვერ მოიძებნა"));
        if (!friendship.getAddressee().getId().equals(currentUserId) || friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new FriendshipException("მოთხოვნა ვერ მოიძებნა");
        }
        return friendship;
    }

    private boolean involves(Friendship friendship, Long userId) {
        return friendship.getRequester().getId().equals(userId) || friendship.getAddressee().getId().equals(userId);
    }

    private User otherParty(Friendship friendship, Long userId) {
        return friendship.getRequester().getId().equals(userId) ? friendship.getAddressee() : friendship.getRequester();
    }
}
