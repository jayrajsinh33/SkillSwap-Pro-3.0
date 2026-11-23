package com.skillswap.repo;
import com.skillswap.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface ChatMessageRepository extends JpaRepository<ChatMessage,Long> {
    List<ChatMessage> findTop150ByConversationIdOrderByCreatedAtAsc(Long conversationId);
}
