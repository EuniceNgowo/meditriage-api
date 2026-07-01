package com.example.triage_api.repository;

import com.example.triage_api.model.SymptomSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;


@Repository
public interface SymptomSessionRepository extends JpaRepository<SymptomSession, UUID> {


    List<SymptomSession> findByUserUserIdOrderByStartedAtDesc(UUID userId);
}
