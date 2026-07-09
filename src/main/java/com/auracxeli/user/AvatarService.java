package com.auracxeli.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AvatarService {
    private static final List<String> AVAILABLE_AVATARS = List.of(
            "avatar-1.png", "avatar-2.png", "avatar-3.png", "avatar-4.png",
            "avatar-5.png", "avatar-6.png", "avatar-7.png", "avatar-8.png",
            "avatar-9.png", "avatar-10.png", "avatar-11.png"
    );
    private final UserRepository userRepository;

    public List<String> availableAvatars() {
        return AVAILABLE_AVATARS;
    }
    @Transactional
    public void chooseAvatar(Long userId, String avatar) {
        if (!AVAILABLE_AVATARS.contains(avatar)) {
            log.warn("Rejected avatar choice for user {}: {} is not in the available set", userId, avatar);
            throw new IllegalArgumentException("არასწორი ავატარი");
        }
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(String.valueOf(userId)));
        user.setAvatar(avatar);
        log.info("User {} chose avatar {}", userId, avatar);
    }
}