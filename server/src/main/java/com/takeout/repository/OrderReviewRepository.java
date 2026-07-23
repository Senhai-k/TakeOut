package com.takeout.repository;

import com.takeout.domain.OrderReview;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OrderReviewRepository extends JpaRepository<OrderReview, Long> {
    Optional<OrderReview> findByOrderId(Long orderId);
}
