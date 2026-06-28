package com.takeout.repository;

import com.takeout.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByIsDeletedOrderByCreatedAtDesc(Integer isDeleted);

    List<Order> findByOrderStatusAndIsDeletedOrderByCreatedAtDesc(Integer orderStatus, Integer isDeleted);

    List<Order> findByShopIdAndIsDeletedOrderByCreatedAtDesc(Long shopId, Integer isDeleted);

    List<Order> findByShopIdAndOrderStatusAndIsDeletedOrderByCreatedAtDesc(
            Long shopId,
            Integer orderStatus,
            Integer isDeleted
    );

    long countByUserIdAndIsDeleted(Long userId, Integer isDeleted);

    @Query("""
            select coalesce(sum(o.payAmount), 0)
            from Order o
            where o.userId = :userId
              and o.isDeleted = 0
              and o.payStatus = :payStatus
              and o.orderStatus <> :cancelledStatus
            """)
    BigDecimal sumPaidAmountByUserId(
            @Param("userId") Long userId,
            @Param("payStatus") Integer payStatus,
            @Param("cancelledStatus") Integer cancelledStatus
    );
}
