package com.skillswap.repo;
import com.skillswap.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface AppUserRepository extends JpaRepository<AppUser,Long> {
    Optional<AppUser> findByEmailIgnoreCase(String email);
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select u from AppUser u where u.id=:id")
    Optional<AppUser> lockById(@org.springframework.data.repository.query.Param("id") Long id);
}
