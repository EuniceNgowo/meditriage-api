package com.example.triage_api.service;

import com.example.triage_api.dto.request.UpdateUserProfileRequest;
import com.example.triage_api.dto.response.UserResponse;
import com.example.triage_api.exception.ResourceNotFoundException;
import com.example.triage_api.model.User;
import com.example.triage_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


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

        return UserResponse.builder()
                .userId(user.getUserId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .preferredLang(user.getPreferredLang().name())
                .createdAt(user.getCreatedAt())
                .build();
    }

    @Transactional
    public UserResponse updateProfile(String userEmail, UpdateUserProfileRequest req) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));
        if (req.getFullName() != null && !req.getFullName().isBlank()) {
            user.setFullName(req.getFullName().trim());
        }
        userRepository.save(user);
        log.info("User {} updated their profile", user.getEmail());
        return getProfile(userEmail);
    }

    @Transactional
    public void deactivate(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));
        user.setIsActive(false);
        userRepository.save(user);
        log.info("User {} account deactivated", user.getEmail());
    }
}
