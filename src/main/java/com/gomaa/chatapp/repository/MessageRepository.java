package com.gomaa.chatapp.repository;

import com.gomaa.chatapp.model.Message;
import com.gomaa.chatapp.model.MessageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends MongoRepository<Message, String> {

    Page<Message> findByConversationIdOrderByCreatedAtDesc(String conversationId, Pageable pageable);

    List<Message> findByConversationIdAndStatusNot(String conversationId, MessageStatus status);

    long countByConversationIdAndStatusNot(String conversationId, MessageStatus status);

    void deleteByConversationId(String conversationId);
}