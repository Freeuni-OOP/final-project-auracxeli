package com.auracxeli.user;

import com.auracxeli.user.dto.RegisterRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

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
}
