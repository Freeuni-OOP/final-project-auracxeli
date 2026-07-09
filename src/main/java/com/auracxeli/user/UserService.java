package com.auracxeli.user;

import com.auracxeli.user.dto.RegisterRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;



    @Transactional
    public User register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateUserException("username", "ეს მომხმარებლის სახელი დაკავებულია");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateUserException("email", "ეს ელფოსტა უკვე რეგისტრირებულია");
        }

        String hashed = passwordEncoder.encode(request.password());
        User user = new User(request.username(), request.email(), hashed);
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
    }

    /** Flips the user's theme between LIGHT and DARK and returns the new value. */
    @Transactional
    public Theme toggleTheme(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + userId));
        Theme next = user.getThemePreference() == Theme.DARK ? Theme.LIGHT : Theme.DARK;
        user.setThemePreference(next);
        userRepository.save(user);
        return next;
    }
}
