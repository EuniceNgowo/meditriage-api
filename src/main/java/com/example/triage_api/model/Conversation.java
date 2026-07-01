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
@Table(name = "conversations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"patient", "doctor", "messages", "linkedSession"})
@ToString(exclude = {"patient", "doctor", "messages", "linkedSession"})
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "conversation_id", updatable = false, nullable = false)
    private UUID conversationId;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private com.example.triage_api.model.Doctor doctor;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "linked_session_id")
    private com.example.triage_api.model.SymptomSession linkedSession;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ConversationStatus status = ConversationStatus.PENDING;


    @Column(name = "chief_complaint", columnDefinition = "TEXT")
    private String chiefComplaint;


    @Column(name = "accepted_at")
    private Instant acceptedAt;


    @Column(name = "closed_at")
    private Instant closedAt;


    @Column(name = "patient_rating")
    private Integer patientRating;


    @Column(name = "patient_feedback", columnDefinition = "TEXT")
    private String patientFeedback;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;


    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL,
               fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderBy("sentAt ASC")   // always load messages in chronological order
    @Builder.Default
    private List<Message> messages = new ArrayList<>();
}
