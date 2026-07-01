package com.example.triage_api.service;

import com.example.triage_api.dto.request.SymptomEntryRequest;
import com.example.triage_api.dto.response.SessionResponse;
import com.example.triage_api.dto.response.SymptomEntryResponse;
import com.example.triage_api.exception.BadRequestException;
import com.example.triage_api.exception.ResourceNotFoundException;
import com.example.triage_api.model.SymptomEntry;
import com.example.triage_api.model.SymptomSession;
import com.example.triage_api.model.User;
import com.example.triage_api.repository.SymptomEntryRepository;
import com.example.triage_api.repository.SymptomSessionRepository;
import com.example.triage_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService {

    private final SymptomSessionRepository sessionRepository;
    private final SymptomEntryRepository entryRepository;
    private final UserRepository userRepository;


    @Transactional
    public SessionResponse createSession(String userEmail) {
        User user = findUserByEmail(userEmail);

        SymptomSession session = SymptomSession.builder()
                .user(user)
                .status("OPEN")
                .build();

        SymptomSession saved = sessionRepository.saveAndFlush(session);
        log.info("Session created: {} for user: {}", saved.getSessionId(), userEmail);

        return mapToSessionResponse(saved);
    }


    @Transactional
    public SymptomEntryResponse addEntry(UUID sessionId,
                                         SymptomEntryRequest request,
                                         String userEmail) {
        SymptomSession session = findSessionOwnedByUser(sessionId, userEmail);

        if (!"OPEN".equals(session.getStatus())) {
            throw new BadRequestException(
                    "Cannot add symptoms to a session with status: " + session.getStatus() +
                    ". Only OPEN sessions accept new entries.");
        }

        SymptomEntry entry = SymptomEntry.builder()
                .session(session)
                .symptomText(request.getSymptomText())
                .severity(request.getSeverity())
                .durationText(request.getDurationText())
                .build();

        SymptomEntry saved = entryRepository.save(entry);
        log.info("Symptom entry added to session {}: '{}'", sessionId, request.getSymptomText());

        return mapToEntryResponse(saved);
    }


    @Transactional(readOnly = true)
    public List<SessionResponse> getUserSessions(String userEmail) {
        User user = findUserByEmail(userEmail);

        return sessionRepository
                .findByUserUserIdOrderByStartedAtDesc(user.getUserId())
                .stream()
                .map(this::mapToSessionResponse)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public SymptomSession getSessionOwnedByUser(UUID sessionId, String userEmail) {
        return findSessionOwnedByUser(sessionId, userEmail);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private helper methods
    // ─────────────────────────────────────────────────────────────────────────

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with email: " + email));
    }


    private SymptomSession findSessionOwnedByUser(UUID sessionId, String userEmail) {
        SymptomSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Session not found with id: " + sessionId));

        if (!session.getUser().getEmail().equals(userEmail)) {
            // Return 404 instead of 403 to avoid leaking session existence to other users
            throw new ResourceNotFoundException("Session not found with id: " + sessionId);
        }

        return session;
    }

    private SessionResponse mapToSessionResponse(SymptomSession session) {
        return SessionResponse.builder()
                .sessionId(session.getSessionId())
                .status(session.getStatus())
                .startedAt(session.getStartedAt())
                .endedAt(session.getEndedAt())
                .entryCount(session.getEntries().size())
                .hasTriageResult(session.getTriageResult() != null)
                .build();
    }

    private SymptomEntryResponse mapToEntryResponse(SymptomEntry entry) {
        return SymptomEntryResponse.builder()
                .entryId(entry.getEntryId())
                .symptomText(entry.getSymptomText())
                .severity(entry.getSeverity())
                .durationText(entry.getDurationText())
                .recordedAt(entry.getRecordedAt())
                .build();
    }
}
