package com.example.triage_api.service;

import com.example.triage_api.dto.request.LoginRequest;
import com.example.triage_api.dto.request.RegisterRequest;
import com.example.triage_api.dto.response.JwtResponse;
import com.example.triage_api.dto.response.MessageResponse;
import com.example.triage_api.exception.BadRequestException;
import com.example.triage_api.model.Doctor;
import com.example.triage_api.model.Language;
import com.example.triage_api.model.User;
import com.example.triage_api.repository.DoctorRepository;
import com.example.triage_api.repository.UserRepository;
import com.example.triage_api.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;


    @Transactional
    public MessageResponse register(RegisterRequest request) {
        // Step 1: Check for duplicate email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException(
                    "An account with email '" + request.getEmail() + "' already exists.");
        }

        // Step 2: Parse preferred language (default to EN if null/invalid)
        Language lang = Language.EN;
        if (request.getPreferredLang() != null) {
            try {
                lang = Language.valueOf(request.getPreferredLang().toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Unknown language code '{}', defaulting to EN", request.getPreferredLang());
            }
        }

        // Step 3: Build and save the User entity with a BCrypt-hashed password
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword())) // BCrypt hash
                .preferredLang(lang)
                .isActive(true)
                .build();

        userRepository.save(user);

        log.info("New user registered: {}", user.getEmail());
        return MessageResponse.builder()
                .content("Registration successful. You can now log in.")
                .build();
    }


    @Transactional(readOnly = true)
    public JwtResponse login(LoginRequest request) {
        /*
         * Trigger Spring Security's authentication pipeline.
         * This internally:
         *   1. Calls UserDetailsServiceImpl.loadUserByUsername(email) → gets stored hash
         *   2. Calls passwordEncoder.matches(inputPassword, storedHash) → compares
         *   3. Throws BadCredentialsException if they don't match
         */
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().toLowerCase().trim(),
                        request.getPassword()
                )
        );

        // Authentication succeeded — generate a JWT token for this user
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtTokenProvider.generateToken(userDetails);

        // Look up the display name to include in the response
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow();

        log.info("User logged in: {}", user.getEmail());

        return JwtResponse.builder()
                .token(token)
                .type("Bearer")
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role("USER")
                .build();
    }


    @Transactional(readOnly = true)
    public JwtResponse doctorLogin(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().toLowerCase().trim(),
                        request.getPassword()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtTokenProvider.generateToken(userDetails);

        Doctor doctor = doctorRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new com.example.triage_api.exception.ResourceNotFoundException(
                        "Doctor not found: " + userDetails.getUsername()));

        log.info("Doctor logged in: {}", doctor.getEmail());

        return JwtResponse.builder()
                .token(token)
                .type("Bearer")
                .email(doctor.getEmail())
                .fullName(doctor.getFullName())
                .role("DOCTOR")
                .doctorId(doctor.getDoctorId())
                .build();
    }
}
