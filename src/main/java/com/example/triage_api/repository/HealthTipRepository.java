package com.example.triage_api.repository;

import com.example.triage_api.model.HealthTip;
import com.example.triage_api.model.TriageLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;


@Repository
public interface HealthTipRepository extends JpaRepository<HealthTip, UUID> {


    List<HealthTip> findByTriageLevel(TriageLevel triageLevel);
}
