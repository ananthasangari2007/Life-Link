package com.lifelink.security;

import com.lifelink.user.model.User;
import com.lifelink.user.model.enums.UserRole;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SecurityUtils {

    public Optional<User> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof SecurityUser securityUser) {
            return Optional.of(securityUser.getUser());
        }
        return Optional.empty();
    }

    public Optional<Long> getCurrentUserId() {
        return getCurrentUser().map(User::getId);
    }

    public Optional<Long> getCurrentFacilityId() {
        return getCurrentUser().map(u -> u.getFacility() != null ? u.getFacility().getId() : null);
    }

    public Optional<UserRole> getCurrentUserRole() {
        return getCurrentUser().map(User::getRole);
    }

    public boolean isCurrentUserAdmin() {
        return getCurrentUserRole().filter(r -> r == UserRole.NETWORK_ADMIN).isPresent();
    }
}
