package com.takeout.repository;

import com.takeout.domain.Dish;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DishRepository extends JpaRepository<Dish, Long> {

    List<Dish> findByShopIdAndStatusAndIsDeleted(Long shopId, Integer status, Integer isDeleted);

    List<Dish> findByCategoryIdAndStatusAndIsDeleted(Long categoryId, Integer status, Integer isDeleted);

    List<Dish> findByShopIdAndIsDeletedOrderByCreatedAtDesc(Long shopId, Integer isDeleted);
}
