package com.takeout.repository;

import com.takeout.domain.Shop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShopRepository extends JpaRepository<Shop, Long> {

    Optional<Shop> findFirstByStatusAndIsDeleted(Integer status, Integer isDeleted);
}
