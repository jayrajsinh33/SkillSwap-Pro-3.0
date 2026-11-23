package com.skillswap.repo;
import com.skillswap.model.Withdrawal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface WithdrawalRepository extends JpaRepository<Withdrawal,Long> {
    List<Withdrawal> findByUserIdOrderByCreatedAtDesc(Long userId);
}
