package com.skillswap.repo;
import com.skillswap.model.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface SupportTicketRepository extends JpaRepository<SupportTicket,Long> {
 List<SupportTicket> findByUserIdOrderByCreatedAtDesc(Long userId);
 List<SupportTicket> findAllByOrderByCreatedAtDesc();
}
