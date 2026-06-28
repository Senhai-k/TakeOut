package com.takeout.repository;

import com.takeout.domain.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<CartItem> findByUserIdAndShopId(Long userId, Long shopId);

    List<CartItem> findByUserIdAndIdIn(Long userId, Collection<Long> ids);

    Optional<CartItem> findByIdAndUserId(Long id, Long userId);

    void deleteByUserId(Long userId);

    void deleteByIdAndUserId(Long id, Long userId);
}
