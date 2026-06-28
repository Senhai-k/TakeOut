package com.takeout.service;

import com.takeout.common.ErrorCode;
import com.takeout.domain.CartItem;
import com.takeout.domain.Dish;
import com.takeout.domain.Shop;
import com.takeout.dto.app.CartItemRequest;
import com.takeout.dto.app.CartItemResponse;
import com.takeout.dto.app.CartItemUpdateRequest;
import com.takeout.dto.app.CartResponse;
import com.takeout.exception.BusinessException;
import com.takeout.repository.CartItemRepository;
import com.takeout.repository.DishRepository;
import com.takeout.repository.ShopRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class AppCartService {

    private static final Long DEFAULT_USER_ID = 1L;

    private final CartItemRepository cartItemRepository;
    private final DishRepository dishRepository;
    private final ShopRepository shopRepository;

    public AppCartService(
            CartItemRepository cartItemRepository,
            DishRepository dishRepository,
            ShopRepository shopRepository
    ) {
        this.cartItemRepository = cartItemRepository;
        this.dishRepository = dishRepository;
        this.shopRepository = shopRepository;
    }

    public CartResponse getCart() {
        return toCartResponse(cartItemRepository.findByUserIdOrderByCreatedAtDesc(DEFAULT_USER_ID));
    }

    @Transactional
    public CartItemResponse addItem(CartItemRequest request) {
        Shop shop = getShop(request.shopId());
        Dish dish = getDish(request.dishId());
        if (!dish.getShopId().equals(shop.getId())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "商品不属于当前店铺");
        }
        if (dish.getStock() <= 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "商品已售罄");
        }

        int quantity = request.quantity() == null ? 1 : request.quantity();
        LocalDateTime now = LocalDateTime.now();
        CartItem item = findSameItem(shop.getId(), dish.getId(), request);
        if (item == null) {
            item = new CartItem();
            item.setUserId(DEFAULT_USER_ID);
            item.setShopId(shop.getId());
            item.setDishId(dish.getId());
            item.setCreatedAt(now);
            item.setQuantity(0);
            item.setSelected(1);
        }
        item.setDishName(dish.getName());
        item.setDishImageUrl(dish.getImageUrl());
        item.setDishPrice(dish.getPrice());
        item.setSizeOption(blankToNull(request.size()));
        item.setSpiceOption(blankToNull(request.spice()));
        item.setNotes(blankToNull(request.notes()));
        item.setQuantity(item.getQuantity() + quantity);
        item.setUpdatedAt(now);
        return toItemResponse(cartItemRepository.save(item));
    }

    @Transactional
    public CartItemResponse updateItem(Long id, CartItemUpdateRequest request) {
        CartItem item = getCartItem(id);
        if (request.quantity() != null) {
            item.setQuantity(request.quantity());
        }
        if (request.selected() != null) {
            item.setSelected(Boolean.TRUE.equals(request.selected()) ? 1 : 0);
        }
        item.setUpdatedAt(LocalDateTime.now());
        return toItemResponse(cartItemRepository.save(item));
    }

    @Transactional
    public void deleteItem(Long id) {
        cartItemRepository.deleteByIdAndUserId(id, DEFAULT_USER_ID);
    }

    @Transactional
    public void clearCart() {
        cartItemRepository.deleteByUserId(DEFAULT_USER_ID);
    }

    List<CartItem> getCartItemsByIds(List<Long> ids) {
        return cartItemRepository.findByUserIdAndIdIn(DEFAULT_USER_ID, ids);
    }

    @Transactional
    void deleteItems(List<CartItem> items) {
        cartItemRepository.deleteAll(items);
    }

    private CartItem findSameItem(Long shopId, Long dishId, CartItemRequest request) {
        return cartItemRepository.findByUserIdAndShopId(DEFAULT_USER_ID, shopId)
                .stream()
                .filter(item -> item.getDishId().equals(dishId))
                .filter(item -> Objects.equals(item.getSizeOption(), blankToNull(request.size())))
                .filter(item -> Objects.equals(item.getSpiceOption(), blankToNull(request.spice())))
                .filter(item -> Objects.equals(item.getNotes(), blankToNull(request.notes())))
                .findFirst()
                .orElse(null);
    }

    private CartItem getCartItem(Long id) {
        return cartItemRepository.findByIdAndUserId(id, DEFAULT_USER_ID)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "购物车商品不存在"));
    }

    private Shop getShop(Long id) {
        return shopRepository.findById(id)
                .filter(shop -> shop.getStatus() == 1 && shop.getIsDeleted() == 0)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "店铺不存在"));
    }

    private Dish getDish(Long id) {
        return dishRepository.findById(id)
                .filter(dish -> dish.getStatus() == 1 && dish.getIsDeleted() == 0)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "商品不存在"));
    }

    private CartResponse toCartResponse(List<CartItem> items) {
        List<CartItemResponse> responses = items.stream()
                .map(this::toItemResponse)
                .toList();
        BigDecimal goodsAmount = responses.stream()
                .map(CartItemResponse::subtotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal deliveryFee = items.isEmpty()
                ? BigDecimal.ZERO
                : getShop(items.get(0).getShopId()).getDeliveryFee();
        return new CartResponse(responses, goodsAmount, deliveryFee, goodsAmount.add(deliveryFee));
    }

    private CartItemResponse toItemResponse(CartItem item) {
        BigDecimal subtotalAmount = item.getDishPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        return new CartItemResponse(
                item.getId(),
                item.getShopId(),
                item.getDishId(),
                item.getDishName(),
                item.getDishImageUrl(),
                item.getDishPrice(),
                item.getQuantity(),
                item.getSelected() == 1,
                subtotalAmount,
                item.getSizeOption(),
                item.getSpiceOption(),
                item.getNotes()
        );
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
