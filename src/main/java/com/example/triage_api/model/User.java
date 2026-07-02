package com.example.triage_api.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = "sessions")
@ToString(exclude = "sessions")
public class User {


    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id", updatable = false, nullable = false)
    private UUID userId;


    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;


    @Column(nullable = false, unique = true, length = 255)
    private String email;


    @Column(name = "password_hash", nullable = false)
    private String passwordHash;


    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_lang", nullable = false, length = 10)
    @Builder.Default
    private Language preferredLang = Language.EN;


    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;


    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;


    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;


    @Column(name = "phone_number", length = 20, unique = true)
    private String phoneNumber;

    @Column(name = "reset_token", length = 6)
    private String resetToken;

    @Column(name = "reset_token_expires_at")
    private Instant resetTokenExpiresAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL,
               fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<SymptomSession> sessions = new ArrayList<>();
}
