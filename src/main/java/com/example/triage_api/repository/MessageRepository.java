package com.example.triage_api.repository;

import com.example.triage_api.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;


@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {


    List<Message> findByConversationConversationIdOrderBySentAtAsc(UUID conversationId);

    Optional<Message> findTopByConversationConversationIdOrderBySentAtDesc(UUID conversationId);


    @Query("""
           SELECT COUNT(m) FROM Message m
           WHERE m.conversation.conversationId = :convId
           AND m.senderType = :senderType
           AND m.isRead = false
           """)
    long countUnread(
            @Param("convId") UUID conversationId,
            @Param("senderType") String senderType);


    @Modifying
    @Query("""
           UPDATE Message m SET m.isRead = true
           WHERE m.conversation.conversationId = :convId
           AND m.senderType = :senderType
           AND m.isRead = false
           """)
    void markAsRead(
            @Param("convId") UUID conversationId,
            @Param("senderType") String senderType);
}
