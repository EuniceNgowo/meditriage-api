package com.example.triage_api.repository;

import com.example.triage_api.model.TriageResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;


@Repository
public interface TriageResultRepository extends JpaRepository<TriageResult, UUID> {


    Optional<TriageResult> findBySessionSessionId(UUID sessionId);
}
