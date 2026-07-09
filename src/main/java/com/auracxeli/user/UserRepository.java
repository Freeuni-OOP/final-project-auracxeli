package com.auracxeli.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // დუპლიკატების შემოწმება რომ დაგვჭირდება, ეს ორი მეთოდი გამოგვადგება.
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    Optional<User> findByUsername(String username);

    List<User> findAllByOrderByUsernameAsc();

    List<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrderByUsernameAsc(
            String username, String email);
}
