package com.example.triage_api.service;

import com.example.triage_api.dto.request.RateConversationRequest;
import com.example.triage_api.dto.request.SendMessageRequest;
import com.example.triage_api.dto.request.StartConversationRequest;
import com.example.triage_api.dto.response.ConversationResponse;
import com.example.triage_api.dto.response.MessageResponse;
import com.example.triage_api.exception.BadRequestException;
import com.example.triage_api.exception.ResourceNotFoundException;
import com.example.triage_api.model.*;
import com.example.triage_api.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final SymptomSessionRepository sessionRepository;
    private final DoctorService doctorService;

    // ═════════════════════════════════════════════════════════════════════════
    // PATIENT ACTIONS
    // ═════════════════════════════════════════════════════════════════════════


    @Transactional
    public ConversationResponse startConversation(String patientEmail,
                                                   StartConversationRequest request) {
        // Load the patient
        User patient = userRepository.findByEmail(patientEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        // Load and validate the doctor
        Doctor doctor = doctorService.findById(request.getDoctorId());

        if (doctor.getStatus() == DoctorStatus.OFFLINE) {
            throw new BadRequestException(
                    "Dr. " + doctor.getFullName() + " is currently offline and not accepting consultations.");
        }
        if (doctor.getStatus() == DoctorStatus.BUSY) {
            throw new BadRequestException(
                    "Dr. " + doctor.getFullName() + " is currently at full capacity. " +
                    "Please try again later or choose another doctor.");
        }

        // If a PENDING request already exists, return it (idempotent). Block only ACTIVE.
        Optional<Conversation> existing = conversationRepository
                .findOpenConversation(patient.getUserId(), doctor.getDoctorId());
        if (existing.isPresent()) {
            if (existing.get().getStatus() == ConversationStatus.ACTIVE) {
                throw new BadRequestException(
                        "You already have an active consultation with Dr. " + doctor.getFullName() + ".");
            }
            return mapToConversationResponse(existing.get(), "PATIENT");
        }

        // Resolve optional linked session
        SymptomSession linkedSession = null;
        if (request.getLinkedSessionId() != null) {
            linkedSession = sessionRepository.findById(request.getLinkedSessionId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Linked session not found: " + request.getLinkedSessionId()));
            // Verify the session belongs to this patient
            if (!linkedSession.getUser().getEmail().equals(patientEmail)) {
                throw new ResourceNotFoundException(
                        "Linked session not found: " + request.getLinkedSessionId());
            }
        }

        // Build and save the conversation
        Conversation conversation = Conversation.builder()
                .patient(patient)
                .doctor(doctor)
                .linkedSession(linkedSession)
                .status(ConversationStatus.PENDING)
                .chiefComplaint(request.getChiefComplaint())
                .build();

        Conversation saved = conversationRepository.save(conversation);
        log.info("Consultation requested by {} with Dr. {}",
                patient.getFullName(), doctor.getFullName());

        return mapToConversationResponse(saved, "PATIENT");
    }


    @Transactional(readOnly = true)
    public Page<ConversationResponse> getPatientConversations(String patientEmail, Pageable pageable) {
        User patient = userRepository.findByEmail(patientEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        return conversationRepository
                .findByPatientUserIdOrderByUpdatedAtDesc(patient.getUserId(), pageable)
                .map(c -> mapToConversationResponse(c, "PATIENT"));
    }


    @Transactional
    public List<MessageResponse> getMessagesAsPatient(UUID conversationId, String patientEmail) {
        User patient = userRepository.findByEmail(patientEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        // Ownership check: returns empty if conversation doesn't belong to this patient
        Conversation conversation = conversationRepository
                .findByConversationIdAndPatientUserId(conversationId, patient.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Conversation not found: " + conversationId));

        // Mark doctor messages as read (patient is now reading them)
        messageRepository.markAsRead(conversationId, "DOCTOR");

        return messageRepository
                .findByConversationConversationIdOrderBySentAtAsc(conversationId)
                .stream()
                .map(m -> mapToMessageResponse(m, conversation))
                .collect(Collectors.toList());
    }


    @Transactional
    public MessageResponse sendMessageAsPatient(UUID conversationId,
                                                 String patientEmail,
                                                 SendMessageRequest request) {
        User patient = userRepository.findByEmail(patientEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        Conversation conversation = conversationRepository
                .findByConversationIdAndPatientUserId(conversationId, patient.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Conversation not found: " + conversationId));

        if (conversation.getStatus() != ConversationStatus.ACTIVE) {
            throw new BadRequestException(
                    "Cannot send messages. Conversation status is: " + conversation.getStatus() +
                    ". The doctor must accept the request first.");
        }

        Message message = Message.builder()
                .conversation(conversation)
                .senderType("PATIENT")
                .senderId(patient.getUserId())
                .content(request.getContent())
                .isRead(false)
                .build();

        Message saved = messageRepository.save(message);
        log.debug("Patient {} sent message in conversation {}", patient.getFullName(), conversationId);

        return mapToMessageResponse(saved, conversation);
    }


    @Transactional
    public ConversationResponse closeConversationAsPatient(UUID conversationId, String patientEmail) {
        User patient = userRepository.findByEmail(patientEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        Conversation conversation = conversationRepository
                .findByConversationIdAndPatientUserId(conversationId, patient.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Conversation not found: " + conversationId));

        if (conversation.getStatus() == ConversationStatus.CLOSED) {
            throw new BadRequestException("This conversation is already closed.");
        }

        conversation.setStatus(ConversationStatus.CLOSED);
        conversation.setClosedAt(Instant.now());
        conversationRepository.save(conversation);

        // Refresh doctor's busy status now that this conversation is closed
        doctorService.refreshDoctorBusyStatus(conversation.getDoctor().getDoctorId());

        log.info("Conversation {} closed by patient {}", conversationId, patient.getFullName());
        return mapToConversationResponse(conversation, "PATIENT");
    }


    @Transactional
    public ConversationResponse rateConversation(UUID conversationId,
                                                  String patientEmail,
                                                  RateConversationRequest request) {
        User patient = userRepository.findByEmail(patientEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        Conversation conversation = conversationRepository
                .findByConversationIdAndPatientUserId(conversationId, patient.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Conversation not found: " + conversationId));

        if (conversation.getStatus() != ConversationStatus.CLOSED) {
            throw new BadRequestException(
                    "You can only rate a conversation after it has been closed.");
        }
        if (conversation.getPatientRating() != null) {
            throw new BadRequestException("You have already rated this conversation.");
        }

        conversation.setPatientRating(request.getRating());
        conversation.setPatientFeedback(request.getFeedback());
        conversationRepository.save(conversation);

        // Update the doctor's running average rating
        doctorService.updateRating(conversation.getDoctor().getDoctorId(), request.getRating());

        log.info("Patient {} rated conversation {} with {}/5 stars",
                patient.getFullName(), conversationId, request.getRating());

        return mapToConversationResponse(conversation, "PATIENT");
    }

    // ═════════════════════════════════════════════════════════════════════════
    // DOCTOR ACTIONS
    // ═════════════════════════════════════════════════════════════════════════


    @Transactional(readOnly = true)
    public Page<ConversationResponse> getDoctorConversations(String doctorEmail, Pageable pageable) {
        Doctor doctor = doctorService.findByEmail(doctorEmail);

        return conversationRepository
                .findByDoctorDoctorIdOrderByUpdatedAtDesc(doctor.getDoctorId(), pageable)
                .map(c -> mapToConversationResponse(c, "DOCTOR"));
    }


    @Transactional(readOnly = true)
    public Page<ConversationResponse> getPendingRequests(String doctorEmail, Pageable pageable) {
        Doctor doctor = doctorService.findByEmail(doctorEmail);

        return conversationRepository
                .findByDoctorDoctorIdAndStatusOrderByCreatedAtAsc(
                        doctor.getDoctorId(), ConversationStatus.PENDING, pageable)
                .map(c -> mapToConversationResponse(c, "DOCTOR"));
    }


    @Transactional
    public ConversationResponse acceptConversation(UUID conversationId, String doctorEmail) {
        Conversation conversation = getConversationForDoctor(conversationId, doctorEmail);

        if (conversation.getStatus() != ConversationStatus.PENDING) {
            throw new BadRequestException(
                    "Only PENDING conversations can be accepted. Status is: " +
                    conversation.getStatus());
        }

        conversation.setStatus(ConversationStatus.ACTIVE);
        conversation.setAcceptedAt(Instant.now());
        conversationRepository.save(conversation);

        // Refresh status — doctor may now be at capacity
        doctorService.refreshDoctorBusyStatus(conversation.getDoctor().getDoctorId());

        log.info("Dr. {} accepted conversation {}", doctorEmail, conversationId);
        return mapToConversationResponse(conversation, "DOCTOR");
    }


    @Transactional
    public ConversationResponse declineConversation(UUID conversationId, String doctorEmail) {
        Conversation conversation = getConversationForDoctor(conversationId, doctorEmail);

        if (conversation.getStatus() != ConversationStatus.PENDING) {
            throw new BadRequestException(
                    "Only PENDING conversations can be declined. Status is: " +
                    conversation.getStatus());
        }

        conversation.setStatus(ConversationStatus.DECLINED);
        conversation.setClosedAt(Instant.now());
        conversationRepository.save(conversation);

        log.info("Dr. {} declined conversation {}", doctorEmail, conversationId);
        return mapToConversationResponse(conversation, "DOCTOR");
    }


    @Transactional
    public List<MessageResponse> getMessagesAsDoctor(UUID conversationId, String doctorEmail) {
        Conversation conversation = getConversationForDoctor(conversationId, doctorEmail);

        // Mark patient messages as read (doctor is now reading them)
        messageRepository.markAsRead(conversationId, "PATIENT");

        return messageRepository
                .findByConversationConversationIdOrderBySentAtAsc(conversationId)
                .stream()
                .map(m -> mapToMessageResponse(m, conversation))
                .collect(Collectors.toList());
    }


    @Transactional
    public MessageResponse sendMessageAsDoctor(UUID conversationId,
                                                String doctorEmail,
                                                SendMessageRequest request) {
        Doctor doctor = doctorService.findByEmail(doctorEmail);
        Conversation conversation = getConversationForDoctor(conversationId, doctorEmail);

        if (conversation.getStatus() != ConversationStatus.ACTIVE) {
            throw new BadRequestException(
                    "Cannot send messages. Conversation status is: " + conversation.getStatus());
        }

        Message message = Message.builder()
                .conversation(conversation)
                .senderType("DOCTOR")
                .senderId(doctor.getDoctorId())
                .content(request.getContent())
                .isRead(false)
                .build();

        Message saved = messageRepository.save(message);
        log.debug("Dr. {} sent message in conversation {}", doctor.getFullName(), conversationId);

        return mapToMessageResponse(saved, conversation);
    }


    @Transactional
    public ConversationResponse closeConversationAsDoctor(UUID conversationId, String doctorEmail) {
        Conversation conversation = getConversationForDoctor(conversationId, doctorEmail);

        if (conversation.getStatus() == ConversationStatus.CLOSED) {
            throw new BadRequestException("This conversation is already closed.");
        }

        conversation.setStatus(ConversationStatus.CLOSED);
        conversation.setClosedAt(Instant.now());
        conversationRepository.save(conversation);

        // Doctor now has one fewer active conversation — may move back to AVAILABLE
        doctorService.refreshDoctorBusyStatus(conversation.getDoctor().getDoctorId());

        log.info("Conversation {} closed by Dr. {}", conversationId, doctorEmail);
        return mapToConversationResponse(conversation, "DOCTOR");
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Private helpers
    // ═════════════════════════════════════════════════════════════════════════


    private Conversation getConversationForDoctor(UUID conversationId, String doctorEmail) {
        Doctor doctor = doctorService.findByEmail(doctorEmail);
        return conversationRepository
                .findByConversationIdAndDoctorDoctorId(conversationId, doctor.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Conversation not found: " + conversationId));
    }

    private ConversationResponse mapToConversationResponse(Conversation c, String requesterType) {
        // Count unread messages from the OTHER party
        String otherSenderType = requesterType.equals("PATIENT") ? "DOCTOR" : "PATIENT";
        long unread = messageRepository.countUnread(c.getConversationId(), otherSenderType);

        return ConversationResponse.builder()
                .conversationId(c.getConversationId())
                .patientId(c.getPatient().getUserId())
                .patientName(c.getPatient().getFullName())
                .doctorId(c.getDoctor().getDoctorId())
                .doctorName(c.getDoctor().getFullName())
                .doctorSpecialty(c.getDoctor().getSpecialty())
                .status(c.getStatus().name())
                .chiefComplaint(c.getChiefComplaint())
                .linkedSessionId(c.getLinkedSession() != null
                        ? c.getLinkedSession().getSessionId() : null)
                .unreadCount(unread)
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .acceptedAt(c.getAcceptedAt())
                .closedAt(c.getClosedAt())
                .build();
    }

    private MessageResponse mapToMessageResponse(Message m, Conversation conversation) {
        // Resolve the sender display name
        String senderName = m.getSenderType().equals("PATIENT")
                ? conversation.getPatient().getFullName()
                : conversation.getDoctor().getFullName();

        return MessageResponse.builder()
                .messageId(m.getMessageId())
                .conversationId(conversation.getConversationId())
                .senderType(m.getSenderType())
                .senderId(m.getSenderId())
                .senderName(senderName)
                .content(m.getContent())
                .isRead(m.getIsRead())
                .sentAt(m.getSentAt())
                .build();
    }
}
