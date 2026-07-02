package com.example.triage_api.service;

import com.example.triage_api.dto.request.*;
import com.example.triage_api.dto.response.JwtResponse;
import com.example.triage_api.dto.response.MessageResponse;
import com.example.triage_api.exception.BadRequestException;
import com.example.triage_api.exception.ResourceNotFoundException;
import com.example.triage_api.model.Doctor;
import com.example.triage_api.model.Language;
import com.example.triage_api.model.User;
import com.example.triage_api.repository.DoctorRepository;
import com.example.triage_api.repository.UserRepository;
import com.example.triage_api.security.JwtTokenProvider;
import com.example.triage_api.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final JavaMailSender mailSender;

    @Value("${app.mail.from:noreply@meditriage.app}")
    private String mailFrom;

    // ─── Patient email registration ───────────────────────────────────────

    @Transactional
    public MessageResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException(
                    "An account with email '" + request.getEmail() + "' already exists.");
        }

        Language lang = Language.EN;
        if (request.getPreferredLang() != null) {
            try { lang = Language.valueOf(request.getPreferredLang().toUpperCase()); }
            catch (IllegalArgumentException e) { /* keep EN */ }
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .preferredLang(lang)
                .isActive(true)
                .build();
        userRepository.save(user);

        log.info("New user registered: {}", user.getEmail());
        return MessageResponse.builder().content("Registration successful. You can now log in.").build();
    }

    // ─── Patient phone registration ───────────────────────────────────────

    @Transactional
    public MessageResponse registerByPhone(PatientPhoneRegisterRequest request) {
        String phone = UserDetailsServiceImpl.normalisePhone(request.getPhoneNumber());
        if (phone.isBlank()) throw new BadRequestException("Invalid phone number.");

        if (userRepository.existsByPhoneNumber(phone)) {
            throw new BadRequestException("An account with phone '" + phone + "' already exists.");
        }

        Language lang = Language.EN;
        if (request.getPreferredLang() != null) {
            try { lang = Language.valueOf(request.getPreferredLang().toUpperCase()); }
            catch (IllegalArgumentException e) { /* keep EN */ }
        }

        // synthetic email so all existing email-based logic still works
        String syntheticEmail = "ph_" + phone.replaceAll("[^0-9]", "") + "@triage.local";

        User user = User.builder()
                .fullName(request.getFullName())
                .email(syntheticEmail)
                .phoneNumber(phone)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .preferredLang(lang)
                .isActive(true)
                .build();
        userRepository.save(user);

        log.info("New patient registered via phone: {}", phone);
        return MessageResponse.builder().content("Registration successful. You can now log in with your phone number.").build();
    }

    // ─── Patient email login ──────────────────────────────────────────────

    @Transactional(readOnly = true)
    public JwtResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().toLowerCase().trim(),
                        request.getPassword()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtTokenProvider.generateToken(userDetails);

        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();

        log.info("User logged in: {}", user.getEmail());
        return JwtResponse.builder()
                .token(token).type("Bearer")
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role("USER")
                .build();
    }

    // ─── Patient phone login ──────────────────────────────────────────────

    @Transactional(readOnly = true)
    public JwtResponse loginByPhone(PhoneLoginRequest request) {
        String phone = UserDetailsServiceImpl.normalisePhone(request.getPhoneNumber());
        User user = userRepository.findByPhoneNumber(phone)
                .orElseThrow(() -> new BadRequestException("No account found with this phone number."));

        // authenticate using the stored (synthetic) email so Spring Security stays consistent
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getEmail(), request.getPassword())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtTokenProvider.generateToken(userDetails);

        log.info("Patient logged in via phone: {}", phone);
        return JwtResponse.builder()
                .token(token).type("Bearer")
                .email(null)                         // phone-only account has no real email
                .phoneNumber(user.getPhoneNumber())
                .fullName(user.getFullName())
                .role("USER")
                .build();
    }

    // ─── Doctor email login ───────────────────────────────────────────────

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
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Doctor not found: " + userDetails.getUsername()));

        log.info("Doctor logged in: {}", doctor.getEmail());
        return JwtResponse.builder()
                .token(token).type("Bearer")
                .email(doctor.getEmail())
                .fullName(doctor.getFullName())
                .role("DOCTOR")
                .doctorId(doctor.getDoctorId())
                .build();
    }

    // ─── Doctor phone login ───────────────────────────────────────────────

    @Transactional(readOnly = true)
    public JwtResponse doctorLoginByPhone(PhoneLoginRequest request) {
        String phone = UserDetailsServiceImpl.normalisePhone(request.getPhoneNumber());
        Doctor doctor = doctorRepository.findByPhoneNumber(phone)
                .orElseThrow(() -> new BadRequestException("No doctor account found with this phone number."));

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(doctor.getEmail(), request.getPassword())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtTokenProvider.generateToken(userDetails);

        log.info("Doctor logged in via phone: {}", phone);
        return JwtResponse.builder()
                .token(token).type("Bearer")
                .email(null)
                .phoneNumber(doctor.getPhoneNumber())
                .fullName(doctor.getFullName())
                .role("DOCTOR")
                .doctorId(doctor.getDoctorId())
                .build();
    }

    // ─── Forgot password ──────────────────────────────────────────────────

    @Transactional
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        boolean isDoctor = "DOCTOR".equalsIgnoreCase(request.getRole());

        String otp = generateOtp();
        Instant expires = Instant.now().plus(15, ChronoUnit.MINUTES);

        if (isDoctor) {
            Doctor doctor = doctorRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("No doctor account found with this email."));
            doctorRepository.setResetToken(email, otp, expires);
            sendOtpEmail(email, doctor.getFullName(), otp);
        } else {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("No patient account found with this email."));
            userRepository.setResetToken(email, otp, expires);
            sendOtpEmail(email, user.getFullName(), otp);
        }

        log.info("Password reset OTP sent to {}", email);
        return MessageResponse.builder()
                .content("A 6-digit reset code has been sent to " + email + ". It expires in 15 minutes.")
                .build();
    }

    // ─── Reset password ───────────────────────────────────────────────────

    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        boolean isDoctor = "DOCTOR".equalsIgnoreCase(request.getRole());
        String newHash = passwordEncoder.encode(request.getNewPassword());

        if (isDoctor) {
            Doctor doctor = doctorRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("No doctor account with this email."));
            validateOtp(doctor.getResetToken(), doctor.getResetTokenExpiresAt(), request.getOtp());
            doctorRepository.updatePassword(email, newHash);
        } else {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("No patient account with this email."));
            validateOtp(user.getResetToken(), user.getResetTokenExpiresAt(), request.getOtp());
            userRepository.updatePassword(email, newHash);
        }

        log.info("Password reset successful for {}", email);
        return MessageResponse.builder().content("Password updated successfully. You can now log in.").build();
    }

    // ─── Helpers ──────────────────────────────────────────────────────────

    private String generateOtp() {
        return String.format("%06d", new SecureRandom().nextInt(1_000_000));
    }

    private void validateOtp(String stored, Instant expires, String submitted) {
        if (stored == null || expires == null) {
            throw new BadRequestException("No reset code was requested for this account.");
        }
        if (Instant.now().isAfter(expires)) {
            throw new BadRequestException("The reset code has expired. Please request a new one.");
        }
        if (!stored.equals(submitted)) {
            throw new BadRequestException("Incorrect reset code.");
        }
    }

    private void sendOtpEmail(String to, String name, String otp) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(mailFrom);
            msg.setTo(to);
            msg.setSubject("MediTriage — Password Reset Code");
            msg.setText(
                "Hello " + name + ",\n\n" +
                "Your password reset code is:\n\n" +
                "    " + otp + "\n\n" +
                "This code expires in 15 minutes.\n\n" +
                "If you did not request this, please ignore this email.\n\n" +
                "— MediTriage Team"
            );
            mailSender.send(msg);
        } catch (MailException ex) {
            log.error("Failed to send OTP email to {}: {}", to, ex.getMessage());
            throw new BadRequestException("Could not send reset email. Please check your email address or try later.");
        }
    }
}
