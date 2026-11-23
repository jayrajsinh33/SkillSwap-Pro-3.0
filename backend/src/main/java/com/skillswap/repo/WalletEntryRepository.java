package com.skillswap.repo;
import com.skillswap.model.WalletEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface WalletEntryRepository extends JpaRepository<WalletEntry,Long> {
    List<WalletEntry> findByUserIdOrderByCreatedAtDesc(Long userId);
}
