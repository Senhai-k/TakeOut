package com.takeout.repository;

import com.takeout.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByShopIdAndStatusAndIsDeletedOrderBySortAsc(Long shopId, Integer status, Integer isDeleted);

    List<Category> findByShopIdAndIsDeletedOrderBySortAsc(Long shopId, Integer isDeleted);
}
