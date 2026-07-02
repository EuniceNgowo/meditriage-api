package com.example.triage_api.security;

import com.example.triage_api.model.Doctor;
import com.example.triage_api.model.User;
import com.example.triage_api.repository.DoctorRepository;
import com.example.triage_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;

    @Override
    public UserDetails loadUserByUsername(String identifier)
            throws UsernameNotFoundException {

        // Check patient table — first by email, then by phone
        java.util.Optional<User> userOpt = identifier.contains("@")
                ? userRepository.findByEmail(identifier)
                : userRepository.findByPhoneNumber(normalisePhone(identifier));

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return org.springframework.security.core.userdetails.User
                    .withUsername(user.getEmail())   // JWT subject is always the stored email
                    .password(user.getPasswordHash())
                    .authorities("ROLE_USER")
                    .disabled(!user.getIsActive())
                    .build();
        }

        // Fall back to doctor table
        Doctor doctor = identifier.contains("@")
                ? doctorRepository.findByEmail(identifier).orElse(null)
                : doctorRepository.findByPhoneNumber(normalisePhone(identifier)).orElse(null);

        if (doctor == null) {
            throw new UsernameNotFoundException("No account found with identifier: " + identifier);
        }

        return org.springframework.security.core.userdetails.User
                .withUsername(doctor.getEmail())     // JWT subject is always the stored email
                .password(doctor.getPasswordHash())
                .authorities("ROLE_DOCTOR")
                .disabled(!doctor.getIsActive())
                .build();
    }

    /** Strip non-digit characters then re-prepend '+' if original started with it */
    static String normalisePhone(String phone) {
        if (phone == null) return "";
        String stripped = phone.strip();
        if (stripped.startsWith("+")) {
            return "+" + stripped.substring(1).replaceAll("[^0-9]", "");
        }
        return stripped.replaceAll("[^0-9]", "");
    }
}
