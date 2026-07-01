package com.example.triage_api.repository;

import com.example.triage_api.model.SymptomEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;


@Repository
public interface SymptomEntryRepository extends JpaRepository<SymptomEntry, UUID> {


    List<SymptomEntry> findBySessionSessionId(UUID sessionId);
}
