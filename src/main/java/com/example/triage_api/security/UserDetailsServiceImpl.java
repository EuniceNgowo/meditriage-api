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
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        // Check patient table first
        java.util.Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return org.springframework.security.core.userdetails.User
                    .withUsername(user.getEmail())
                    .password(user.getPasswordHash())
                    .authorities("ROLE_USER")
                    .disabled(!user.getIsActive())
                    .build();
        }

        // Fall back to doctor table
        Doctor doctor = doctorRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "No account found with email: " + email));

        return org.springframework.security.core.userdetails.User
                .withUsername(doctor.getEmail())
                .password(doctor.getPasswordHash())
                .authorities("ROLE_DOCTOR")
                .disabled(!doctor.getIsActive())
                .build();
    }
}