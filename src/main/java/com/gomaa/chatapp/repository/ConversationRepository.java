package com.gomaa.chatapp.repository;

import com.gomaa.chatapp.model.Conversation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends MongoRepository<Conversation, String> {

    List<Conversation> findByParticipantIdsContainingOrderByUpdatedAtDesc(String userId);

    @Query("{ 'type': 'DIRECT', 'participantIds': { $all: [?0, ?1] }, $expr: { $eq: [{ $size: '$participantIds' }, 2] } }")
    Optional<Conversation> findDirectConversation(String userId1, String userId2);

    boolean existsByIdAndParticipantIdsContaining(String id, String userId);
}

