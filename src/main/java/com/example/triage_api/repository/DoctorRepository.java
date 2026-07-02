package com.example.triage_api.repository;

import com.example.triage_api.model.Doctor;
import com.example.triage_api.model.DoctorStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;


@Repository
public interface DoctorRepository extends JpaRepository<Doctor, UUID> {

    Optional<Doctor> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<Doctor> findByPhoneNumber(String phoneNumber);

    boolean existsByPhoneNumber(String phoneNumber);

    Page<Doctor> findByStatusAndIsActiveTrueOrderByRatingAverageDesc(DoctorStatus status, Pageable pageable);

    @Query(value = "SELECT d FROM Doctor d WHERE LOWER(d.specialty) LIKE LOWER(CONCAT('%', :specialty, '%')) AND d.isActive = true ORDER BY d.ratingAverage DESC NULLS LAST",
           countQuery = "SELECT COUNT(d) FROM Doctor d WHERE LOWER(d.specialty) LIKE LOWER(CONCAT('%', :specialty, '%')) AND d.isActive = true")
    Page<Doctor> findBySpecialtyContainingIgnoreCase(@Param("specialty") String specialty, Pageable pageable);

    @Query(value = "SELECT d FROM Doctor d WHERE d.isActive = true ORDER BY d.ratingAverage DESC NULLS LAST, d.fullName ASC",
           countQuery = "SELECT COUNT(d) FROM Doctor d WHERE d.isActive = true")
    Page<Doctor> findAllActiveDoctors(Pageable pageable);

    @Query("SELECT COUNT(c) FROM Conversation c WHERE c.doctor.doctorId = :doctorId AND c.status = 'ACTIVE'")
    long countActiveConversations(@Param("doctorId") UUID doctorId);

    /** Bypass entity save to avoid Hibernate orphan-removal on conversations collection */
    /** Bypass entity save to avoid Hibernate orphan-removal on conversations collection */
    @Modifying
    @Query(value = """
        UPDATE doctors SET
          full_name        = COALESCE(:fullName,   full_name),
          specialty        = COALESCE(:specialty,  specialty),
          bio              = COALESCE(:bio,         bio),
          languages_spoken = COALESCE(:langs,       languages_spoken),
          years_experience = COALESCE(:yrs,         years_experience)
        WHERE email = :email
        """, nativeQuery = true)
    int updateProfile(
        @Param("email")    String  email,
        @Param("fullName") String  fullName,
        @Param("specialty") String specialty,
        @Param("bio")      String  bio,
        @Param("langs")    String  langs,
        @Param("yrs")      Integer yrs
    );

    @Modifying
    @Query(value = "UPDATE doctors SET is_active = false, status = 'OFFLINE' WHERE email = :email", nativeQuery = true)
    int deactivateByEmail(@Param("email") String email);

    @Modifying
    @Query(value = "UPDATE doctors SET is_active = false, status = 'OFFLINE' WHERE doctor_id = :id", nativeQuery = true)
    int deactivateById(@Param("id") UUID id);

    @Modifying
    @Query(value = "UPDATE doctors SET reset_token = :token, reset_token_expires_at = :expires WHERE email = :email", nativeQuery = true)
    int setResetToken(@Param("email") String email, @Param("token") String token, @Param("expires") java.time.Instant expires);

    @Modifying
    @Query(value = "UPDATE doctors SET password_hash = :hash, reset_token = NULL, reset_token_expires_at = NULL WHERE email = :email", nativeQuery = true)
    int updatePassword(@Param("email") String email, @Param("hash") String hash);
}
