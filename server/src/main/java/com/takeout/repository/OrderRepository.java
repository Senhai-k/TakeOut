package com.takeout.repository;

import com.takeout.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
