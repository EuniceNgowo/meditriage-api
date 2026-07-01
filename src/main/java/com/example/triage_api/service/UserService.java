package com.example.triage_api.service;

import com.example.triage_api.dto.response.UserResponse;
import com.example.triage_api.exception.ResourceNotFoundException;
import com.example.triage_api.model.User;
import com.example.triage_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
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
}
