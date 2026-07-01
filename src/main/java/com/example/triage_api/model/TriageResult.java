package com.example.triage_api.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * TriageResult — stores the AI-generated triage analysis for one SymptomSession.
 *
 * After the user submits symptoms and calls POST /api/sessions/{id}/triage,
 * the backend sends the symptoms to OpenAI and stores the parsed result here.
 *
 * Database table: triage_results
 */
@Entity
@Table(name = "triage_results")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"session", "healthTips"})
@ToString(exclude = {"session", "healthTips"})
public class TriageResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "result_id", updatable = false, nullable = false)
    private UUID resultId;


    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false, unique = true)
    private SymptomSession session;


    @Enumerated(EnumType.STRING)
    @Column(name = "triage_level", nullable = false, length = 10)
    private TriageLevel triageLevel;


    @Column(name = "ai_confidence")
    private Double aiConfidence;


    @Column(name = "probable_conditions", columnDefinition = "TEXT")
    private String probableConditions;


    @Column(name = "escalation_advice", columnDefinition = "TEXT")
    private String escalationAdvice;


    @Column(name = "raw_ai_response", columnDefinition = "TEXT")
    private String rawAiResponse;


    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;


    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "result_tips",
        joinColumns = @JoinColumn(name = "result_id"),
        inverseJoinColumns = @JoinColumn(name = "tip_id")
    )
    @Builder.Default
    private List<HealthTip> healthTips = new ArrayList<>();
}
