package com.auracxeli.admin;

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
    public List<User> listUsers() {
        return userRepository.findAllByOrderByUsernameAsc();
    }

    @Transactional(readOnly = true)
    public List<User> listUsers(String query) {
        String normalizedQuery = query == null ? "" : query.trim();
        if (normalizedQuery.isEmpty()) {
            return listUsers();
        }
        return userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrderByUsernameAsc(
                normalizedQuery, normalizedQuery);
    }

    @Transactional
    public User toggleActive(Long targetUserId, Long adminUserId) {
        User target = findTarget(targetUserId);
        validateManageable(target, adminUserId);
        target.setActive(!target.isActive());
        return userRepository.save(target);
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
}
