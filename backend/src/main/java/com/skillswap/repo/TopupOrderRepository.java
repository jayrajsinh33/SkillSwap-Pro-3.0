package com.skillswap.repo;
import com.skillswap.model.TopupOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface TopupOrderRepository extends JpaRepository<TopupOrder,Long> {
    Optional<TopupOrder> findByGatewayOrderId(String gatewayOrderId);
}
