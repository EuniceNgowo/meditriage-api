package com.example.triage_api.repository;

import com.example.triage_api.model.Conversation;
import com.example.triage_api.model.ConversationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;


@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {


    Page<Conversation> findByPatientUserIdOrderByUpdatedAtDesc(UUID patientId, Pageable pageable);


    Page<Conversation> findByDoctorDoctorIdOrderByUpdatedAtDesc(UUID doctorId, Pageable pageable);


    Page<Conversation> findByDoctorDoctorIdAndStatusOrderByCreatedAtAsc(
            UUID doctorId, ConversationStatus status, Pageable pageable);


    Optional<Conversation> findByConversationIdAndPatientUserId(
            UUID conversationId, UUID patientId);


    Optional<Conversation> findByConversationIdAndDoctorDoctorId(
            UUID conversationId, UUID doctorId);


    @Query("""
           SELECT COUNT(c) > 0 FROM Conversation c
           WHERE c.patient.userId = :patientId
           AND c.doctor.doctorId = :doctorId
           AND c.status IN ('PENDING', 'ACTIVE')
           """)
    boolean existsOpenConversation(
            @Param("patientId") UUID patientId,
            @Param("doctorId") UUID doctorId);

    @Query("""
           SELECT c FROM Conversation c
           WHERE c.patient.userId = :patientId
           AND c.doctor.doctorId = :doctorId
           AND c.status IN ('PENDING', 'ACTIVE')
           ORDER BY c.createdAt DESC
           LIMIT 1
           """)
    Optional<Conversation> findOpenConversation(
            @Param("patientId") UUID patientId,
            @Param("doctorId") UUID doctorId);
}
