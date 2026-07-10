package com.auracxeli.admin;

import com.auracxeli.admin.dto.UserRow;
import com.auracxeli.user.Role;
import com.auracxeli.user.User;
import com.auracxeli.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<UserRow> listUsers() {
        return userRepository.findAllByOrderByUsernameAsc().stream()
                .map(AdminUserService::toRow)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserRow> listUsers(String query) {
        String normalizedQuery = query == null ? "" : query.trim();
        if (normalizedQuery.isEmpty()) {
            return listUsers();
        }
        return userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrderByUsernameAsc(
                        normalizedQuery, normalizedQuery).stream()
                .map(AdminUserService::toRow)
                .toList();
    }

    @Transactional
    public UserRow toggleActive(Long targetUserId, Long adminUserId) {
        User target = findTarget(targetUserId);
        validateManageable(target, adminUserId);
        target.setActive(!target.isActive());
        return toRow(userRepository.save(target));
    }

    @Transactional
    public void deleteUser(Long targetUserId, Long adminUserId) {
        User target = findTarget(targetUserId);
        validateManageable(target, adminUserId);
        userRepository.delete(target);
    }

    private User findTarget(Long targetUserId) {
        return userRepository.findById(targetUserId)
                .orElseThrow(() -> new AdminUserActionDeniedException("მომხმარებელი ვერ მოიძებნა"));
    }

    private void validateManageable(User target, Long adminUserId) {
        if (Objects.equals(target.getId(), adminUserId)) {
            throw new AdminUserActionDeniedException("საკუთარი ანგარიშის დაბლოკვა/წაშლა აკრძალულია");
        }
        if (target.getRole() == Role.ADMIN) {
            throw new AdminUserActionDeniedException("ადმინის ანგარიშის დაბლოკვა/წაშლა აკრძალულია");
        }
    }

    private static UserRow toRow(User user) {
        return new UserRow(user.getId(), user.getUsername(), user.getEmail(), user.getRole(), user.isActive());
    }
}
