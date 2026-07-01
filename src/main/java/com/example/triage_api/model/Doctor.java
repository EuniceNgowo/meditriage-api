package com.example.triage_api.model;
import lombok.Data;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Entity
@Table(name = "doctors")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = "conversations")
@ToString(exclude = "conversations")
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "doctor_id", updatable = false, nullable = false)
    private UUID doctorId;


    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;


    @Column(nullable = false, unique = true, length = 255)
    private String email;


    @Column(name = "password_hash", nullable = false)
    private String passwordHash;


    @Column(nullable = false, length = 120)
    private String specialty;


    @Column(name = "license_number", nullable = false, length = 80)
    private String licenseNumber;


    @Column(columnDefinition = "TEXT")
    private String bio;


    @Column(name = "years_experience")
    private Integer yearsExperience;


    @Column(name = "languages_spoken", length = 100)
    @Builder.Default
    private String languagesSpoken = "EN";


    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private DoctorStatus status = DoctorStatus.OFFLINE;


    @Column(name = "rating_average")
    private Double ratingAverage;


    @Column(name = "rating_count")
    @Builder.Default
    private Integer ratingCount = 0;


    @Column(name = "max_active_chats", nullable = false)
    @Builder.Default
    private Integer maxActiveChats = 5;


    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;


    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL,
               fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<Conversation> conversations = new ArrayList<>();
}
