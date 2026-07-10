package com.auracxeli.admin.dto;

import com.auracxeli.user.Role;

/**
 * View row for the admin user list. Deliberately omits the bcrypt password
 * (and every other sensitive/lazy field) that the {@code User} entity carries.
 */
public record UserRow(
        Long id,
        String username,
        String email,
        Role role,
        boolean active
) {
}
