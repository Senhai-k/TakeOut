package com.takeout.service;

import com.takeout.common.ErrorCode;
import com.takeout.common.PageResult;
import com.takeout.domain.CartItem;
import com.takeout.domain.Category;
import com.takeout.domain.Dish;
import com.takeout.domain.Order;
import com.takeout.domain.OrderItem;
import com.takeout.domain.Shop;
import com.takeout.domain.UserAddress;
import com.takeout.domain.enums.OrderStatus;
import com.takeout.domain.enums.PayStatus;
import com.takeout.dto.app.CategoryDishResponse;
import com.takeout.dto.app.CreateOrderRequest;
import com.takeout.dto.app.CreateOrderResponse;
import com.takeout.dto.app.DishResponse;
import com.takeout.dto.app.OrderDetailResponse;
import com.takeout.dto.app.OrderItemResponse;
import com.takeout.dto.app.ShopResponse;
import com.takeout.exception.BusinessException;
import com.takeout.repository.CategoryRepository;
import com.takeout.repository.DishRepository;
import com.takeout.repository.OrderItemRepository;
import com.takeout.repository.OrderRepository;
import com.takeout.repository.ShopRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AppMockService {

    private static final DateTimeFormatter ORDER_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final Long DEFAULT_USER_ID = 1L;

    private final ShopRepository shopRepository;
    private final CategoryRepository categoryRepository;
    private final DishRepository dishRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final AppAddressService appAddressService;
    private final AppCartService appCartService;

    public AppMockService(
            ShopRepository shopRepository,
            CategoryRepository categoryRepository,
            DishRepository dishRepository,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            AppAddressService appAddressService,
            AppCartService appCartService
    ) {
        this.shopRepository = shopRepository;
        this.categoryRepository = categoryRepository;
        this.dishRepository = dishRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.appAddressService = appAddressService;
        this.appCartService = appCartService;
    }

    public ShopResponse getShop(Long shopId) {
        return toShopResponse(shopId == null ? getAvailableShop() : getShopById(shopId));
    }

    public List<CategoryDishResponse> listDishes(Long shopId) {
        Shop shop = shopId == null ? getAvailableShop() : getShopById(shopId);
        return categoryRepository.findByShopIdAndStatusAndIsDeletedOrderBySortAsc(shop.getId(), 1, 0)
                .stream()
                .map(category -> new CategoryDishResponse(
                        category.getId(),
                        category.getName(),
                        category.getSort(),
                        dishRepository.findByCategoryIdAndStatusAndIsDeleted(category.getId(), 1, 0)
                                .stream()
                                .map(this::toDishResponse)
                                .toList()
                ))
                .toList();
    }

    public DishResponse getDish(Long id) {
        return toDishResponse(findDish(id));
    }

    @Transactional
    public CreateOrderResponse createOrder(CreateOrderRequest request) {
        Shop shop = getShopById(request.shopId());
        if (shop.getBusinessStatus() != 1) {
            throw new BusinessException(ErrorCode.CONFLICT, "店铺休息中");
        }

        UserAddress address = appAddressService.getAddressEntity(request.addressId());
        List<CartItem> cartItems = appCartService.getCartItemsByIds(request.cartItemIds());
        if (cartItems.size() != request.cartItemIds().size()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "购物车商品不存在");
        }
        if (cartItems.stream().anyMatch(item -> !item.getShopId().equals(shop.getId()))) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "购物车商品不属于当前店铺");
        }
        List<OrderItemResponse> itemSnapshots = cartItems.stream()
                .map(item -> toOrderItemResponse(findDish(item.getDishId()), item))
                .toList();
        BigDecimal goodsAmount = itemSnapshots.stream()
                .map(OrderItemResponse::subtotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (goodsAmount.compareTo(shop.getMinOrderAmount()) < 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "商品金额未达到起送价");
        }

        LocalDateTime now = LocalDateTime.now();
        Order order = new Order();
        order.setOrderNo("TMP" + System.nanoTime());
        order.setUserId(DEFAULT_USER_ID);
        order.setShopId(shop.getId());
        order.setAddressId(request.addressId());
        order.setReceiverName(address.getReceiverName());
        order.setReceiverPhone(address.getReceiverPhone());
        order.setReceiverAddress(formatAddress(address));
        order.setRemark(request.remark());
        order.setGoodsAmount(goodsAmount);
        order.setDeliveryFee(shop.getDeliveryFee());
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setPayAmount(goodsAmount.add(shop.getDeliveryFee()));
        order.setPayStatus(PayStatus.UNPAID.getCode());
        order.setOrderStatus(OrderStatus.UNPAID.getCode());
        order.setIsDeleted(0);
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        Order savedOrder = orderRepository.save(order);
        savedOrder.setOrderNo("TO" + now.format(ORDER_NO_FORMATTER) + savedOrder.getId());
        savedOrder = orderRepository.save(savedOrder);

        Order finalOrder = savedOrder;
        List<OrderItem> orderItems = itemSnapshots.stream()
                .map(item -> toOrderItemEntity(finalOrder, item, now))
                .toList();
        orderItemRepository.saveAll(orderItems);
        appCartService.deleteItems(cartItems);

        return new CreateOrderResponse(
                savedOrder.getId(),
                savedOrder.getOrderNo(),
                savedOrder.getPayAmount(),
                savedOrder.getOrderStatus()
        );
    }

    public PageResult<OrderDetailResponse> listOrders(Integer status, long page, long pageSize) {
        List<Order> orders = status == null
                ? orderRepository.findByIsDeletedOrderByCreatedAtDesc(0)
                : orderRepository.findByOrderStatusAndIsDeletedOrderByCreatedAtDesc(status, 0);
        long safePage = Math.max(page, 1);
        long safePageSize = Math.max(pageSize, 1);
        int fromIndex = (int) Math.min((safePage - 1) * safePageSize, orders.size());
        int toIndex = (int) Math.min(fromIndex + safePageSize, orders.size());
        List<OrderDetailResponse> records = orders.subList(fromIndex, toIndex)
                .stream()
                .map(this::toOrderDetailResponse)
                .toList();
        return new PageResult<>(records, orders.size(), safePage, safePageSize);
    }

    public OrderDetailResponse getOrderDetail(Long id) {
        Order order = orderRepository.findById(id)
                .filter(item -> item.getIsDeleted() == 0)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "订单不存在"));
        return toOrderDetailResponse(order);
    }

    @Transactional
    public OrderDetailResponse mockPay(Long id) {
        Order order = getOrderEntity(id);
        if (!order.getOrderStatus().equals(OrderStatus.UNPAID.getCode())) {
            throw new BusinessException(ErrorCode.CONFLICT, "当前订单状态不能支付");
        }
        order.setPayStatus(PayStatus.PAID.getCode());
        order.setOrderStatus(OrderStatus.PAID_WAIT_ACCEPT.getCode());
        order.setUpdatedAt(LocalDateTime.now());
        return toOrderDetailResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderDetailResponse cancelOrder(Long id) {
        Order order = getOrderEntity(id);
        if (order.getOrderStatus() >= OrderStatus.DELIVERING.getCode()) {
            throw new BusinessException(ErrorCode.CONFLICT, "当前订单状态不能取消");
        }
        order.setOrderStatus(OrderStatus.CANCELLED.getCode());
        order.setUpdatedAt(LocalDateTime.now());
        return toOrderDetailResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderDetailResponse completeOrder(Long id) {
        Order order = getOrderEntity(id);
        if (!order.getOrderStatus().equals(OrderStatus.DELIVERING.getCode())) {
            throw new BusinessException(ErrorCode.CONFLICT, "当前订单状态不能确认收货");
        }
        order.setOrderStatus(OrderStatus.COMPLETED.getCode());
        order.setUpdatedAt(LocalDateTime.now());
        return toOrderDetailResponse(orderRepository.save(order));
    }

    private Shop getAvailableShop() {
        return shopRepository.findFirstByStatusAndIsDeleted(1, 0)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "店铺不存在"));
    }

    private Order getOrderEntity(Long id) {
        return orderRepository.findById(id)
                .filter(item -> item.getIsDeleted() == 0)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "订单不存在"));
    }

    private Shop getShopById(Long shopId) {
        return shopRepository.findById(shopId)
                .filter(shop -> shop.getStatus() == 1 && shop.getIsDeleted() == 0)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "店铺不存在"));
    }

    private Dish findDish(Long id) {
        return dishRepository.findById(id)
                .filter(dish -> dish.getStatus() == 1 && dish.getIsDeleted() == 0)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "商品不存在"));
    }

    private OrderDetailResponse toOrderDetailResponse(Order order) {
        Shop shop = getShopById(order.getShopId());
        List<OrderItemResponse> items = orderItemRepository.findByOrderId(order.getId())
                .stream()
                .map(this::toOrderItemResponse)
                .toList();
        return new OrderDetailResponse(
                order.getId(),
                order.getOrderNo(),
                order.getShopId(),
                shop.getName(),
                order.getReceiverName(),
                order.getReceiverPhone(),
                order.getReceiverAddress(),
                order.getRemark(),
                order.getGoodsAmount(),
                order.getDeliveryFee(),
                order.getDiscountAmount(),
                order.getPayAmount(),
                order.getPayStatus(),
                order.getOrderStatus(),
                items,
                order.getCreatedAt()
        );
    }

    private ShopResponse toShopResponse(Shop shop) {
        return new ShopResponse(
                shop.getId(),
                shop.getName(),
                shop.getLogoUrl(),
                shop.getNotice(),
                shop.getPhone(),
                shop.getAddress(),
                shop.getMinOrderAmount(),
                shop.getDeliveryFee(),
                shop.getBusinessStatus()
        );
    }

    private DishResponse toDishResponse(Dish dish) {
        return new DishResponse(
                dish.getId(),
                dish.getShopId(),
                dish.getCategoryId(),
                dish.getName(),
                dish.getImageUrl(),
                dish.getDescription(),
                dish.getPrice(),
                dish.getStock(),
                dish.getSalesCount(),
                dish.getStatus()
        );
    }

    private OrderItemResponse toOrderItemResponse(Dish dish, int quantity) {
        if (dish.getStock() < quantity) {
            throw new BusinessException(ErrorCode.CONFLICT, "商品库存不足");
        }
        BigDecimal subtotalAmount = dish.getPrice().multiply(BigDecimal.valueOf(quantity));
        dish.setStock(dish.getStock() - quantity);
        dish.setSalesCount(dish.getSalesCount() + quantity);
        dish.setUpdatedAt(LocalDateTime.now());
        dishRepository.save(dish);
        return new OrderItemResponse(
                dish.getId(),
                dish.getName(),
                dish.getImageUrl(),
                dish.getPrice(),
                quantity,
                null,
                null,
                null,
                subtotalAmount
        );
    }

    private OrderItemResponse toOrderItemResponse(Dish dish, CartItem cartItem) {
        if (dish.getStock() < cartItem.getQuantity()) {
            throw new BusinessException(ErrorCode.CONFLICT, "商品库存不足");
        }
        BigDecimal subtotalAmount = dish.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
        dish.setStock(dish.getStock() - cartItem.getQuantity());
        dish.setSalesCount(dish.getSalesCount() + cartItem.getQuantity());
        dish.setUpdatedAt(LocalDateTime.now());
        dishRepository.save(dish);
        return new OrderItemResponse(
                dish.getId(),
                dish.getName(),
                dish.getImageUrl(),
                dish.getPrice(),
                cartItem.getQuantity(),
                cartItem.getSizeOption(),
                cartItem.getSpiceOption(),
                cartItem.getNotes(),
                subtotalAmount
        );
    }

    private OrderItemResponse toOrderItemResponse(OrderItem item) {
        return new OrderItemResponse(
                item.getDishId(),
                item.getDishName(),
                item.getDishImageUrl(),
                item.getDishPrice(),
                item.getQuantity(),
                item.getSizeOption(),
                item.getSpiceOption(),
                item.getNotes(),
                item.getSubtotalAmount()
        );
    }

    private OrderItem toOrderItemEntity(Order order, OrderItemResponse item, LocalDateTime now) {
        OrderItem orderItem = new OrderItem();
        orderItem.setOrderId(order.getId());
        orderItem.setOrderNo(order.getOrderNo());
        orderItem.setDishId(item.dishId());
        orderItem.setDishName(item.dishName());
        orderItem.setDishImageUrl(item.dishImageUrl());
        orderItem.setDishPrice(item.dishPrice());
        orderItem.setQuantity(item.quantity());
        orderItem.setSizeOption(item.size());
        orderItem.setSpiceOption(item.spice());
        orderItem.setNotes(item.notes());
        orderItem.setSubtotalAmount(item.subtotalAmount());
        orderItem.setCreatedAt(now);
        return orderItem;
    }

    private String formatAddress(UserAddress address) {
        return String.join("",
                nullToEmpty(address.getProvince()),
                nullToEmpty(address.getCity()),
                nullToEmpty(address.getDistrict()),
                nullToEmpty(address.getDetail()),
                address.getHouseNumber() == null || address.getHouseNumber().isBlank()
                        ? ""
                        : " " + address.getHouseNumber()
        );
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
