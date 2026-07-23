package com.takeout.repository;

import com.takeout.domain.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    Optional<PaymentTransaction> findByRequestId(String requestId);
    Optional<PaymentTransaction> findFirstByOrderIdAndStatusOrderByIdDesc(Long orderId, String status);
}
