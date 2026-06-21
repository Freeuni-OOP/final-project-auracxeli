package com.auracxeli.user;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    // დუპლიკატების შემოწმება რომ დაგვჭირდება, ეს ორი მეთოდი გამოგვადგება.
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}