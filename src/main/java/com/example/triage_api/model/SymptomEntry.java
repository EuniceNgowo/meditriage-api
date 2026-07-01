package com.example.triage_api.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;


@Entity
@Table(name = "symptom_entries")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = "session")
@ToString(exclude = "session")
public class SymptomEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "entry_id", updatable = false, nullable = false)
    private UUID entryId;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private SymptomSession session;


    @Column(name = "symptom_text", nullable = false, columnDefinition = "TEXT")
    private String symptomText;


    @Column(name = "severity")
    private Integer severity;


    @Column(name = "duration_text", length = 100)
    private String durationText;


    @CreationTimestamp
    @Column(name = "recorded_at", updatable = false, nullable = false)
    private Instant recordedAt;
}
