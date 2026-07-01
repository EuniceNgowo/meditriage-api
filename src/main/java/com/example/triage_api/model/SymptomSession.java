package com.example.triage_api.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Entity
@Table(name = "symptom_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"user", "entries", "triageResult"})
@ToString(exclude = {"user", "entries", "triageResult"})
public class SymptomSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "session_id", updatable = false, nullable = false)
    private UUID sessionId;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "OPEN";


    @CreationTimestamp
    @Column(name = "started_at", updatable = false, nullable = false)
    private Instant startedAt;


    @Column(name = "ended_at")
    private Instant endedAt;


    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL,
               fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<SymptomEntry> entries = new ArrayList<>();


    @OneToOne(mappedBy = "session", cascade = CascadeType.ALL,
              fetch = FetchType.LAZY, orphanRemoval = true)
    private TriageResult triageResult;
}
