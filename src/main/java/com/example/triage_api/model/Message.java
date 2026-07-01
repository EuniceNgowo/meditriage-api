package com.example.triage_api.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;


@Entity
@Table(name = "messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = "conversation")
@ToString(exclude = "conversation")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "message_id", updatable = false, nullable = false)
    private UUID messageId;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;


    @Column(name = "sender_type", nullable = false, length = 10)
    private String senderType;


    @Column(name = "sender_id", nullable = false)
    private UUID senderId;


    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;


    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;


    @CreationTimestamp
    @Column(name = "sent_at", updatable = false, nullable = false)
    private Instant sentAt;
}
