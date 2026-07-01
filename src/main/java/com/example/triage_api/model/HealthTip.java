package com.example.triage_api.model;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;


@Entity
@Table(name = "health_tips")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HealthTip {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "tip_id", updatable = false, nullable = false)
    private UUID tipId;


    @Column(nullable = false, length = 200)
    private String title;


    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;


    @Enumerated(EnumType.STRING)
    @Column(name = "triage_level", nullable = false, length = 10)
    private TriageLevel triageLevel;


    @Column(length = 80)
    private String category;
}
