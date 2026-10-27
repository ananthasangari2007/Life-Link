package com.lifelink.user.service;

import com.lifelink.common.exception.ResourceNotFoundException;
import com.lifelink.security.SecurityUtils;
import com.lifelink.user.dto.UserResponse;
import com.lifelink.user.model.User;
import com.lifelink.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;
    private final AuthService authService;

    public UserService(UserRepository userRepository, SecurityUtils securityUtils, AuthService authService) {
        this.userRepository = userRepository;
        this.securityUtils = securityUtils;
        this.authService = authService;
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUserProfile() {
        User user = securityUtils.getCurrentUser()
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user session not found."));
        return authService.toUserResponse(user);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> getFacilityUsers(Long facilityId, Pageable pageable) {
        return userRepository.findByFacilityId(facilityId, pageable).map(authService::toUserResponse);
    }
}
