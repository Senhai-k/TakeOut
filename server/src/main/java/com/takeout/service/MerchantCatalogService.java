package com.takeout.service;

import com.takeout.common.ErrorCode;
import com.takeout.common.PageResult;
import com.takeout.domain.Category;
import com.takeout.domain.Dish;
import com.takeout.dto.merchant.MerchantCategoryResponse;
import com.takeout.dto.merchant.MerchantCategoryUpsertRequest;
import com.takeout.dto.merchant.MerchantDishResponse;
import com.takeout.dto.merchant.MerchantDishUpsertRequest;
import com.takeout.exception.BusinessException;
import com.takeout.repository.CategoryRepository;
import com.takeout.repository.DishRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MerchantCatalogService {

    private static final Long DEFAULT_SHOP_ID = 1L;

    private final CategoryRepository categoryRepository;
    private final DishRepository dishRepository;

    public MerchantCatalogService(CategoryRepository categoryRepository, DishRepository dishRepository) {
        this.categoryRepository = categoryRepository;
        this.dishRepository = dishRepository;
    }

    public PageResult<MerchantCategoryResponse> listCategories() {
        List<MerchantCategoryResponse> records = categoryRepository.findByShopIdAndIsDeletedOrderBySortAsc(DEFAULT_SHOP_ID, 0)
                .stream()
                .map(this::toCategoryResponse)
                .toList();
        long size = Math.max(records.size(), 1);
        return new PageResult<>(records, records.size(), 1, size);
    }

    @Transactional
    public MerchantCategoryResponse saveCategory(Long id, MerchantCategoryUpsertRequest request) {
        LocalDateTime now = LocalDateTime.now();
        Category category = id == null
                ? new Category()
                : categoryRepository.findById(id)
                .filter(item -> item.getIsDeleted() == 0)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "分类不存在"));
        if (category.getShopId() != null && !DEFAULT_SHOP_ID.equals(category.getShopId())) {
            throw new BusinessException(ErrorCode.CONFLICT, "无权限修改该分类");
        }
        if (category.getId() == null) {
            category.setId(nextCategoryId());
            category.setCreatedAt(now);
        }
        category.setShopId(DEFAULT_SHOP_ID);
        category.setName(request.name().trim());
        category.setSort(request.sort());
        category.setStatus(request.status());
        category.setIsDeleted(0);
        category.setUpdatedAt(now);
        categoryRepository.save(category);
        return toCategoryResponse(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .filter(item -> item.getIsDeleted() == 0)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "分类不存在"));
        category.setIsDeleted(1);
        category.setUpdatedAt(LocalDateTime.now());
        categoryRepository.save(category);
    }

    public PageResult<MerchantDishResponse> listDishes() {
        List<MerchantDishResponse> records = dishRepository.findByShopIdAndIsDeletedOrderByCreatedAtDesc(DEFAULT_SHOP_ID, 0)
                .stream()
                .map(this::toDishResponse)
                .toList();
        long size = Math.max(records.size(), 1);
        return new PageResult<>(records, records.size(), 1, size);
    }

    @Transactional
    public MerchantDishResponse saveDish(Long id, MerchantDishUpsertRequest request) {
        LocalDateTime now = LocalDateTime.now();
        Dish dish = id == null
                ? new Dish()
                : dishRepository.findById(id)
                .filter(item -> item.getIsDeleted() == 0)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "商品不存在"));
        Category category = categoryRepository.findById(request.categoryId())
                .filter(item -> item.getIsDeleted() == 0)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "分类不存在"));
        if (!DEFAULT_SHOP_ID.equals(category.getShopId())) {
            throw new BusinessException(ErrorCode.CONFLICT, "商品分类不属于当前店铺");
        }
        if (dish.getId() == null) {
            dish.setId(nextDishId());
            dish.setSalesCount(0);
            dish.setCreatedAt(now);
        }
        dish.setShopId(DEFAULT_SHOP_ID);
        dish.setCategoryId(request.categoryId());
        dish.setName(request.name().trim());
        dish.setImageUrl(request.imageUrl());
        dish.setDescription(request.description());
        dish.setPrice(request.price());
        dish.setStock(request.stock());
        dish.setStatus(request.status());
        dish.setIsDeleted(0);
        dish.setUpdatedAt(now);
        dishRepository.save(dish);
        return toDishResponse(dish);
    }

    @Transactional
    public void deleteDish(Long id) {
        Dish dish = dishRepository.findById(id)
                .filter(item -> item.getIsDeleted() == 0)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "商品不存在"));
        dish.setIsDeleted(1);
        dish.setUpdatedAt(LocalDateTime.now());
        dishRepository.save(dish);
    }

    @Transactional
    public MerchantDishResponse updateDishStatus(Long id, Integer status) {
        Dish dish = dishRepository.findById(id)
                .filter(item -> item.getIsDeleted() == 0)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "商品不存在"));
        dish.setStatus(status);
        dish.setUpdatedAt(LocalDateTime.now());
        dishRepository.save(dish);
        return toDishResponse(dish);
    }

    private Long nextCategoryId() {
        return categoryRepository.findAll().stream()
                .mapToLong(Category::getId)
                .max()
                .orElse(0L) + 1;
    }

    private Long nextDishId() {
        return dishRepository.findAll().stream()
                .mapToLong(Dish::getId)
                .max()
                .orElse(0L) + 1;
    }

    private MerchantCategoryResponse toCategoryResponse(Category category) {
        return new MerchantCategoryResponse(
                category.getId(),
                category.getShopId(),
                category.getName(),
                category.getSort(),
                category.getStatus()
        );
    }

    private MerchantDishResponse toDishResponse(Dish dish) {
        String categoryName = categoryRepository.findById(dish.getCategoryId())
                .map(Category::getName)
                .orElse("");
        return new MerchantDishResponse(
                dish.getId(),
                dish.getShopId(),
                dish.getCategoryId(),
                categoryName,
                dish.getName(),
                dish.getImageUrl(),
                dish.getDescription(),
                dish.getPrice(),
                dish.getStock(),
                dish.getSalesCount(),
                dish.getStatus()
        );
    }
}
