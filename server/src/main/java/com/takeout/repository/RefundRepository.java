package com.takeout.repository;

import com.takeout.domain.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RefundRepository extends JpaRepository<Refund, Long> {
    Optional<Refund> findByRequestId(String requestId);
    Optional<Refund> findByOrderId(Long orderId);
}
