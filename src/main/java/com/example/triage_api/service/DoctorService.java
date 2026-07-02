package com.example.triage_api.service;

import com.example.triage_api.dto.request.DoctorPhoneRegisterRequest;
import com.example.triage_api.dto.request.DoctorRegisterRequest;
import com.example.triage_api.dto.request.UpdateDoctorProfileRequest;
import com.example.triage_api.security.UserDetailsServiceImpl;
import com.example.triage_api.dto.response.DoctorResponse;
import com.example.triage_api.exception.BadRequestException;
import com.example.triage_api.exception.ResourceNotFoundException;
import com.example.triage_api.model.Doctor;
import com.example.triage_api.model.DoctorStatus;
import com.example.triage_api.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;

    // ─── Registration ─────────────────────────────────────────────────────


    @Transactional
    public com.example.triage_api.dto.response.JwtResponse register(
            DoctorRegisterRequest request) {

        if (doctorRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException(
                    "A doctor account with email '" + request.getEmail() + "' already exists.");
        }

        Doctor doctor = Doctor.builder()
                .fullName(request.getFullName())
                .email(request.getEmail().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .specialty(request.getSpecialty())
                .licenseNumber(request.getLicenseNumber())
                .bio(request.getBio())
                .yearsExperience(request.getYearsExperience())
                .languagesSpoken(request.getLanguagesSpoken() != null
                        ? request.getLanguagesSpoken().toUpperCase()
                        : "EN")
                .status(DoctorStatus.OFFLINE)
                .isActive(true)
                .build();

        doctorRepository.save(doctor);
        log.info("Doctor registered: {} ({})", doctor.getFullName(), doctor.getEmail());

        // Return a JwtResponse so the doctor can log in immediately after registration
        return com.example.triage_api.dto.response.JwtResponse.builder()
                .email(doctor.getEmail())
                .fullName(doctor.getFullName())
                .type("Bearer")
                .token("") // token will be issued by the login flow
                .build();
    }

    @Transactional
    public com.example.triage_api.dto.response.JwtResponse registerByPhone(DoctorPhoneRegisterRequest request) {
        String phone = UserDetailsServiceImpl.normalisePhone(request.getPhoneNumber());
        if (phone.isBlank()) throw new BadRequestException("Invalid phone number.");

        if (doctorRepository.existsByPhoneNumber(phone)) {
            throw new BadRequestException("A doctor account with phone '" + phone + "' already exists.");
        }

        String syntheticEmail = "ph_" + phone.replaceAll("[^0-9]", "") + "@triage.local";

        Doctor doctor = Doctor.builder()
                .fullName(request.getFullName())
                .email(syntheticEmail)
                .phoneNumber(phone)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .specialty(request.getSpecialty())
                .licenseNumber(request.getLicenseNumber())
                .bio(request.getBio())
                .yearsExperience(request.getYearsExperience())
                .languagesSpoken(request.getLanguagesSpoken() != null
                        ? request.getLanguagesSpoken().toUpperCase() : "EN")
                .status(DoctorStatus.OFFLINE)
                .isActive(true)
                .build();

        doctorRepository.save(doctor);
        log.info("Doctor registered via phone: {} ({})", doctor.getFullName(), phone);

        return com.example.triage_api.dto.response.JwtResponse.builder()
                .email(null).phoneNumber(phone)
                .fullName(doctor.getFullName())
                .type("Bearer").token("")
                .build();
    }

    // ─── Browse doctors (patient-facing) ──────────────────────────────────


    @Transactional(readOnly = true)
    public Page<DoctorResponse> getAvailableDoctors(Pageable pageable) {
        return doctorRepository
                .findByStatusAndIsActiveTrueOrderByRatingAverageDesc(DoctorStatus.AVAILABLE, pageable)
                .map(this::mapToResponse);
    }


    @Transactional(readOnly = true)
    public Page<DoctorResponse> getAllDoctors(Pageable pageable) {
        return doctorRepository.findAllActiveDoctors(pageable)
                .map(this::mapToResponse);
    }


    @Transactional(readOnly = true)
    public Page<DoctorResponse> getDoctorsBySpecialty(String specialty, Pageable pageable) {
        return doctorRepository.findBySpecialtyContainingIgnoreCase(specialty, pageable)
                .map(this::mapToResponse);
    }


    @Transactional(readOnly = true)
    public DoctorResponse getDoctorById(UUID doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Doctor not found with id: " + doctorId));
        return mapToResponse(doctor);
    }

    // ─── Doctor self-management ────────────────────────────────────────────


    @Transactional
    public DoctorResponse updateStatus(String doctorEmail, DoctorStatus newStatus) {
        Doctor doctor = findByEmail(doctorEmail);
        doctor.setStatus(newStatus);
        doctorRepository.save(doctor);
        log.info("Doctor {} status updated to {}", doctor.getFullName(), newStatus);
        return mapToResponse(doctor);
    }


    @Transactional(readOnly = true)
    public DoctorResponse getMyProfile(String doctorEmail) {
        return mapToResponse(findByEmail(doctorEmail));
    }

    @Transactional
    public DoctorResponse updateProfile(String doctorEmail, UpdateDoctorProfileRequest req) {
        String fullName  = (req.getFullName() != null && !req.getFullName().isBlank())
                ? req.getFullName().trim() : null;
        String specialty = (req.getSpecialty() != null && !req.getSpecialty().isBlank())
                ? req.getSpecialty().trim() : null;
        String bio       = req.getBio() != null ? req.getBio().trim() : null;
        String langs     = (req.getLanguagesSpoken() != null && !req.getLanguagesSpoken().isBlank())
                ? req.getLanguagesSpoken().toUpperCase().trim() : null;

        int rows = doctorRepository.updateProfile(doctorEmail, fullName, specialty, bio, langs, req.getYearsExperience());
        if (rows == 0) throw new ResourceNotFoundException("Doctor not found: " + doctorEmail);
        log.info("Doctor {} updated their profile", doctorEmail);
        return mapToResponse(findByEmail(doctorEmail));
    }

    @Transactional
    public void deactivate(String doctorEmail) {
        int rows = doctorRepository.deactivateByEmail(doctorEmail);
        if (rows == 0) throw new ResourceNotFoundException("Doctor not found: " + doctorEmail);
        log.info("Doctor {} account deactivated", doctorEmail);
    }

    @Transactional
    public void deactivateById(UUID doctorId) {
        int rows = doctorRepository.deactivateById(doctorId);
        if (rows == 0) throw new ResourceNotFoundException("Doctor not found: " + doctorId);
        log.info("Doctor {} account deactivated by admin", doctorId);
    }

    // ─── Rating update (called by ConversationService) ────────────────────


    @Transactional
    public void updateRating(UUID doctorId, int newRating) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Doctor not found with id: " + doctorId));

        double currentTotal = (doctor.getRatingAverage() != null ? doctor.getRatingAverage() : 0.0)
                * doctor.getRatingCount();
        int newCount = doctor.getRatingCount() + 1;
        double newAverage = (currentTotal + newRating) / newCount;

        doctor.setRatingCount(newCount);
        doctor.setRatingAverage(Math.round(newAverage * 10.0) / 10.0); // round to 1 decimal
        doctorRepository.save(doctor);

        log.info("Doctor {} new rating: {}/5 ({} ratings total)",
                doctor.getFullName(), doctor.getRatingAverage(), doctor.getRatingCount());
    }


    @Transactional
    public void refreshDoctorBusyStatus(UUID doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId).orElseThrow();

        if (doctor.getStatus() == DoctorStatus.OFFLINE) {
            return; // Offline doctors are not auto-switched to BUSY
        }

        long activeCount = doctorRepository.countActiveConversations(doctorId);

        DoctorStatus targetStatus = activeCount >= doctor.getMaxActiveChats()
                ? DoctorStatus.BUSY
                : DoctorStatus.AVAILABLE;

        if (doctor.getStatus() != targetStatus) {
            doctor.setStatus(targetStatus);
            doctorRepository.save(doctor);
            log.info("Doctor {} status auto-updated to {} ({} active chats)",
                    doctor.getFullName(), targetStatus, activeCount);
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────

    public Doctor findByEmail(String email) {
        return doctorRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Doctor not found with email: " + email));
    }

    public Doctor findById(UUID doctorId) {
        return doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Doctor not found with id: " + doctorId));
    }

    private DoctorResponse mapToResponse(Doctor doctor) {
        return DoctorResponse.builder()
                .doctorId(doctor.getDoctorId())
                .fullName(doctor.getFullName())
                .specialty(doctor.getSpecialty())
                .licenseNumber(doctor.getLicenseNumber())
                .bio(doctor.getBio())
                .yearsExperience(doctor.getYearsExperience())
                .languagesSpoken(doctor.getLanguagesSpoken())
                .status(doctor.getStatus().name())
                .ratingAverage(doctor.getRatingAverage())
                .ratingCount(doctor.getRatingCount())
                .build();
    }
}
