package com.example.triage_api.repository;

import com.example.triage_api.model.Doctor;
import com.example.triage_api.model.DoctorStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;


@Repository
public interface DoctorRepository extends JpaRepository<Doctor, UUID> {


    Optional<Doctor> findByEmail(String email);


    boolean existsByEmail(String email);


    Page<Doctor> findByStatusAndIsActiveTrueOrderByRatingAverageDesc(DoctorStatus status, Pageable pageable);


    @Query(value = "SELECT d FROM Doctor d WHERE LOWER(d.specialty) LIKE LOWER(CONCAT('%', :specialty, '%')) AND d.isActive = true ORDER BY d.ratingAverage DESC NULLS LAST",
           countQuery = "SELECT COUNT(d) FROM Doctor d WHERE LOWER(d.specialty) LIKE LOWER(CONCAT('%', :specialty, '%')) AND d.isActive = true")
    Page<Doctor> findBySpecialtyContainingIgnoreCase(@Param("specialty") String specialty, Pageable pageable);


    @Query(value = "SELECT d FROM Doctor d WHERE d.isActive = true ORDER BY d.ratingAverage DESC NULLS LAST, d.fullName ASC",
           countQuery = "SELECT COUNT(d) FROM Doctor d WHERE d.isActive = true")
    Page<Doctor> findAllActiveDoctors(Pageable pageable);


    @Query("SELECT COUNT(c) FROM Conversation c WHERE c.doctor.doctorId = :doctorId AND c.status = 'ACTIVE'")
    long countActiveConversations(@Param("doctorId") UUID doctorId);
}
