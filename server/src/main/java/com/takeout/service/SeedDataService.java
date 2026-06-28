package com.takeout.service;

import com.takeout.domain.AdminAccount;
import com.takeout.domain.Category;
import com.takeout.domain.Dish;
import com.takeout.domain.Order;
import com.takeout.domain.OrderItem;
import com.takeout.domain.Shop;
import com.takeout.domain.UserAddress;
import com.takeout.domain.enums.OrderStatus;
import com.takeout.domain.enums.PayStatus;
import com.takeout.dto.admin.SeedResetResponse;
import com.takeout.repository.AdminAccountRepository;
import com.takeout.repository.CartItemRepository;
import com.takeout.repository.CategoryRepository;
import com.takeout.repository.DishRepository;
import com.takeout.repository.OrderItemRepository;
import com.takeout.repository.OrderRepository;
import com.takeout.repository.ShopRepository;
import com.takeout.repository.UserAddressRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SeedDataService {

    private static final Long DEFAULT_USER_ID = 1L;
    private static final Long DEFAULT_SHOP_ID = 1L;

    private final ShopRepository shopRepository;
    private final CategoryRepository categoryRepository;
    private final DishRepository dishRepository;
    private final UserAddressRepository userAddressRepository;
    private final AdminAccountRepository adminAccountRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PasswordEncoder passwordEncoder;

    public SeedDataService(
            ShopRepository shopRepository,
            CategoryRepository categoryRepository,
            DishRepository dishRepository,
            UserAddressRepository userAddressRepository,
            AdminAccountRepository adminAccountRepository,
            CartItemRepository cartItemRepository,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.shopRepository = shopRepository;
        this.categoryRepository = categoryRepository;
        this.dishRepository = dishRepository;
        this.userAddressRepository = userAddressRepository;
        this.adminAccountRepository = adminAccountRepository;
        this.cartItemRepository = cartItemRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public SeedResetResponse seedIfMissing() {
        LocalDateTime now = LocalDateTime.now();
        shopRepository.saveAll(shops(now));
        categoryRepository.saveAll(categories(now));
        dishRepository.saveAll(dishes(now));
        if (!userAddressRepository.existsById(1L)) {
            userAddressRepository.save(address(now));
        }
        if (!adminAccountRepository.existsById(1L)) {
            adminAccountRepository.save(adminAccount(now));
        }
        return new SeedResetResponse(3, 12, 12, 1, 0);
    }

    @Transactional
    public SeedResetResponse reset() {
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        cartItemRepository.deleteAll();

        LocalDateTime now = LocalDateTime.now();
        shopRepository.saveAll(shops(now));
        categoryRepository.saveAll(categories(now));
        dishRepository.saveAll(dishes(now));
        userAddressRepository.save(address(now));
        adminAccountRepository.save(adminAccount(now));
        int orders = seedOrders(now);
        return new SeedResetResponse(3, 12, 12, 1, orders);
    }

    private int seedOrders(LocalDateTime now) {
        createOrder(
                "D202606280001",
                OrderStatus.PAID_WAIT_ACCEPT,
                PayStatus.PAID,
                "少放洋葱",
                now.minusMinutes(25),
                List.of(orderLine(101L, "玛格丽特披萨", "39.00", 1), orderLine(104L, "冰柠檬茶", "8.00", 2))
        );
        createOrder(
                "D202606280002",
                OrderStatus.ACCEPTED,
                PayStatus.PAID,
                "尽快送达",
                now.minusMinutes(18),
                List.of(orderLine(102L, "榴莲芝士披萨", "48.00", 1))
        );
        createOrder(
                "D202606280003",
                OrderStatus.COOKING,
                PayStatus.PAID,
                "",
                now.minusMinutes(12),
                List.of(orderLine(103L, "黄金炸鸡翅", "22.00", 2), orderLine(104L, "冰柠檬茶", "8.00", 1))
        );
        createOrder(
                "D202606280004",
                OrderStatus.DELIVERING,
                PayStatus.PAID,
                "到楼下电话联系",
                now.minusMinutes(8),
                List.of(orderLine(101L, "玛格丽特披萨", "39.00", 2))
        );
        createOrder(
                "D202606280005",
                OrderStatus.COMPLETED,
                PayStatus.PAID,
                "已完成订单",
                now.minusHours(1),
                List.of(orderLine(103L, "黄金炸鸡翅", "22.00", 1), orderLine(104L, "冰柠檬茶", "8.00", 1))
        );
        return 5;
    }

    private void createOrder(
            String orderNo,
            OrderStatus orderStatus,
            PayStatus payStatus,
            String remark,
            LocalDateTime createdAt,
            List<OrderLine> lines
    ) {
        BigDecimal goodsAmount = lines.stream()
                .map(line -> line.price.multiply(BigDecimal.valueOf(line.quantity)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal deliveryFee = new BigDecimal("4.00");
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(DEFAULT_USER_ID);
        order.setShopId(DEFAULT_SHOP_ID);
        order.setAddressId(1L);
        order.setReceiverName("张三");
        order.setReceiverPhone("13800000000");
        order.setReceiverAddress("广东省深圳市南山区科技园 A 座 1001");
        order.setRemark(remark);
        order.setGoodsAmount(goodsAmount);
        order.setDeliveryFee(deliveryFee);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setPayAmount(goodsAmount.add(deliveryFee));
        order.setPayStatus(payStatus.getCode());
        order.setOrderStatus(orderStatus.getCode());
        order.setIsDeleted(0);
        order.setCreatedAt(createdAt);
        order.setUpdatedAt(createdAt);
        Order saved = orderRepository.save(order);
        orderItemRepository.saveAll(lines.stream()
                .map(line -> toOrderItem(saved, line, createdAt))
                .toList());
    }

    private OrderItem toOrderItem(Order order, OrderLine line, LocalDateTime createdAt) {
        OrderItem item = new OrderItem();
        item.setOrderId(order.getId());
        item.setOrderNo(order.getOrderNo());
        item.setDishId(line.dishId);
        item.setDishName(line.name);
        item.setDishImageUrl("");
        item.setDishPrice(line.price);
        item.setQuantity(line.quantity);
        item.setSizeOption("普通");
        item.setSpiceOption("不辣");
        item.setNotes("");
        item.setSubtotalAmount(line.price.multiply(BigDecimal.valueOf(line.quantity)));
        item.setCreatedAt(createdAt);
        return item;
    }

    private AdminAccount adminAccount(LocalDateTime now) {
        AdminAccount adminAccount = new AdminAccount();
        adminAccount.setId(1L);
        adminAccount.setUsername("admin");
        adminAccount.setPasswordHash(passwordEncoder.encode("123456"));
        adminAccount.setDisplayName("系统管理员");
        adminAccount.setRole("MERCHANT_ADMIN");
        adminAccount.setShopId(DEFAULT_SHOP_ID);
        adminAccount.setShopName("玛利亚披萨");
        adminAccount.setStatus(1);
        adminAccount.setCreatedAt(now);
        adminAccount.setUpdatedAt(now);
        return adminAccount;
    }

    private List<Shop> shops(LocalDateTime now) {
        return List.of(
                shop(1L, "玛利亚披萨", "招牌披萨现烤出餐，高峰期请预留配送时间。", "广东省深圳市南山区科技园", "20.00", "4.00", now),
                shop(2L, "南山拉面馆", "汤面默认分装，辣度可在菜品详情中备注。", "广东省深圳市南山区粤海街道", "20.00", "4.00", now),
                shop(3L, "炭火烧烤铺", "烧烤现点现烤，满 ¥68 赠送冰柠檬茶。", "广东省深圳市南山区后海", "20.00", "5.00", now)
        );
    }

    private Shop shop(Long id, String name, String notice, String address, String minOrderAmount, String deliveryFee, LocalDateTime now) {
        Shop shop = new Shop();
        shop.setId(id);
        shop.setName(name);
        shop.setLogoUrl("");
        shop.setNotice(notice);
        shop.setPhone("13800000000");
        shop.setAddress(address);
        shop.setMinOrderAmount(new BigDecimal(minOrderAmount));
        shop.setDeliveryFee(new BigDecimal(deliveryFee));
        shop.setBusinessStatus(1);
        shop.setStatus(1);
        shop.setIsDeleted(0);
        shop.setCreatedAt(now);
        shop.setUpdatedAt(now);
        return shop;
    }

    private List<Category> categories(LocalDateTime now) {
        return List.of(
                category(101L, 1L, "热销", 1, now),
                category(102L, 1L, "披萨", 2, now),
                category(103L, 1L, "小吃", 3, now),
                category(104L, 1L, "饮品", 4, now),
                category(201L, 2L, "热销", 1, now),
                category(202L, 2L, "拉面", 2, now),
                category(203L, 2L, "小吃", 3, now),
                category(204L, 2L, "饮品", 4, now),
                category(301L, 3L, "热销", 1, now),
                category(302L, 3L, "烧烤", 2, now),
                category(303L, 3L, "主食", 3, now),
                category(304L, 3L, "饮品", 4, now)
        );
    }

    private Category category(Long id, Long shopId, String name, Integer sort, LocalDateTime now) {
        Category category = new Category();
        category.setId(id);
        category.setShopId(shopId);
        category.setName(name);
        category.setSort(sort);
        category.setStatus(1);
        category.setIsDeleted(0);
        category.setCreatedAt(now);
        category.setUpdatedAt(now);
        return category;
    }

    private List<Dish> dishes(LocalDateTime now) {
        return List.of(
                dish(101L, 1L, 101L, "玛格丽特披萨", "番茄酱、罗勒、双重芝士", "39.00", 100, 368, now),
                dish(102L, 1L, 102L, "榴莲芝士披萨", "香甜榴莲果肉，芝士拉满", "48.00", 80, 246, now),
                dish(103L, 1L, 103L, "黄金炸鸡翅", "外酥里嫩，搭配蜂蜜芥末酱", "22.00", 120, 198, now),
                dish(104L, 1L, 104L, "冰柠檬茶", "清爽解腻，适合搭配披萨", "8.00", 200, 421, now),
                dish(201L, 2L, 201L, "麻辣拉面", "浓郁汤底，微辣过瘾", "25.00", 100, 512, now),
                dish(202L, 2L, 202L, "豚骨叉烧拉面", "慢熬豚骨汤，厚切叉烧", "32.00", 100, 386, now),
                dish(203L, 2L, 203L, "日式煎饺", "底部焦香，肉汁饱满", "16.00", 120, 224, now),
                dish(204L, 2L, 204L, "乌龙冷泡茶", "低糖清爽，解辣不腻", "9.00", 160, 168, now),
                dish(301L, 3L, 301L, "招牌羊肉串", "炭火慢烤，孜然香气足", "28.00", 100, 456, now),
                dish(302L, 3L, 302L, "蜜汁烤鸡翅", "甜咸适中，外皮焦香", "24.00", 100, 312, now),
                dish(303L, 3L, 303L, "牛肉炒饭", "粒粒分明，牛肉香足", "22.00", 100, 188, now),
                dish(304L, 3L, 304L, "酸梅汤", "冰镇酸甜，夜宵搭档", "8.00", 160, 236, now)
        );
    }

    private Dish dish(Long id, Long shopId, Long categoryId, String name, String description, String price, Integer stock, Integer salesCount, LocalDateTime now) {
        Dish dish = new Dish();
        dish.setId(id);
        dish.setShopId(shopId);
        dish.setCategoryId(categoryId);
        dish.setName(name);
        dish.setImageUrl("");
        dish.setDescription(description);
        dish.setPrice(new BigDecimal(price));
        dish.setStock(stock);
        dish.setSalesCount(salesCount);
        dish.setStatus(1);
        dish.setIsDeleted(0);
        dish.setCreatedAt(now);
        dish.setUpdatedAt(now);
        return dish;
    }

    private UserAddress address(LocalDateTime now) {
        UserAddress address = new UserAddress();
        address.setId(1L);
        address.setUserId(DEFAULT_USER_ID);
        address.setReceiverName("张三");
        address.setReceiverPhone("13800000000");
        address.setProvince("广东省");
        address.setCity("深圳市");
        address.setDistrict("南山区");
        address.setDetail("科技园");
        address.setHouseNumber("A 座 1001");
        address.setIsDefault(1);
        address.setIsDeleted(0);
        address.setCreatedAt(now);
        address.setUpdatedAt(now);
        return address;
    }

    private OrderLine orderLine(Long dishId, String name, String price, int quantity) {
        return new OrderLine(dishId, name, new BigDecimal(price), quantity);
    }

    private record OrderLine(Long dishId, String name, BigDecimal price, int quantity) {
    }
}
