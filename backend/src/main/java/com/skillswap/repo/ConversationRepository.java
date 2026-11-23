package com.skillswap.repo;
import com.skillswap.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface ConversationRepository extends JpaRepository<Conversation,Long> {
    @org.springframework.data.jpa.repository.Query("select distinct c from Conversation c join c.members m where m.id=:id order by c.createdAt desc")
    List<Conversation> forMember(@org.springframework.data.repository.query.Param("id") Long id);
    Optional<Conversation> findByCourseIdAndType(Long courseId,String type);
}
