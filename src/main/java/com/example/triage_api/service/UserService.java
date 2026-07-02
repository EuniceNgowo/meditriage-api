package com.example.triage_api.service;

import com.example.triage_api.dto.request.UpdateUserProfileRequest;
import com.example.triage_api.dto.response.UserResponse;
import com.example.triage_api.exception.ResourceNotFoundException;
import com.example.triage_api.model.User;
import com.example.triage_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;


    @Transactional(readOnly = true)
    public UserResponse getProfile(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with email: " + userEmail));
        return toResponse(user);
    }

    @Transactional
    public UserResponse updateProfile(String userEmail, UpdateUserProfileRequest req) {
        if (req.getFullName() == null || req.getFullName().isBlank()) {
            return getProfile(userEmail);
        }
        int rows = userRepository.updateFullName(userEmail, req.getFullName().trim());
        if (rows == 0) throw new ResourceNotFoundException("User not found: " + userEmail);
        log.info("User {} updated their profile", userEmail);
        return getProfile(userEmail);
    }

    @Transactional
    public void deactivate(String userEmail) {
        int rows = userRepository.deactivateByEmail(userEmail);
        if (rows == 0) throw new ResourceNotFoundException("User not found: " + userEmail);
        log.info("User {} account deactivated", userEmail);
    }

    @Transactional
    public void changePassword(String userEmail, String newPasswordHash) {
        int rows = userRepository.updatePassword(userEmail, newPasswordHash);
        if (rows == 0) throw new ResourceNotFoundException("User not found: " + userEmail);
    }

    // ─── Admin ────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<UserResponse> listAllPatients(Pageable pageable) {
        return userRepository.findAllUsers(pageable).map(this::toResponse);
    }

    @Transactional
    public void deactivateById(UUID userId) {
        int rows = userRepository.deactivateById(userId);
        if (rows == 0) throw new ResourceNotFoundException("Patient not found: " + userId);
        log.info("Patient {} deactivated by admin", userId);
    }

    // ─── Helper ───────────────────────────────────────────────────────────

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .preferredLang(user.getPreferredLang().name())
                .createdAt(user.getCreatedAt())
                .isActive(user.getIsActive())
                .build();
    }
}
