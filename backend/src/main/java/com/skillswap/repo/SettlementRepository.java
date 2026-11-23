package com.skillswap.repo;
import com.skillswap.model.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface SettlementRepository extends JpaRepository<Settlement,Long>{List<Settlement> findByMentorIdOrderByCreatedAtDesc(Long mentorId);}
