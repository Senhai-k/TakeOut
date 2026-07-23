package com.takeout;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.takeout.security.JwtService;
import com.takeout.repository.DishRepository;
import com.takeout.repository.OrderRepository;
import com.takeout.repository.PaymentTransactionRepository;
import com.takeout.repository.RefundRepository;
import com.takeout.repository.AppUserRepository;
import com.takeout.service.OrderExpirationService;
import com.takeout.domain.enums.OrderStatus;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AppMockApiTests extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @Autowired private DishRepository dishRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private PaymentTransactionRepository paymentTransactionRepository;
    @Autowired private RefundRepository refundRepository;
    @Autowired private AppUserRepository appUserRepository;
    @Autowired private OrderExpirationService orderExpirationService;

    @Test
    void getShopReturnsSuccess() throws Exception {
        mockMvc.perform(get("/api/app/shop"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.name").value("玛利亚披萨"));
    }

    @Test
    void defaultLoginReturnsUserProfile() throws Exception {
        mockMvc.perform(post("/api/app/auth/login"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.nickname").value("张三"))
                .andExpect(jsonPath("$.data.memberLevel").value("黄金会员"))
                .andExpect(jsonPath("$.data.token").isNotEmpty());
    }

    @Test
    void profileStatsReturnsUserOrderSummary() throws Exception {
        Long orderId = createPaidOrder();

        mockMvc.perform(post("/api/merchant/orders/{id}/accept", orderId)
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        updateMerchantOrderStatus(orderId, 40);
        updateMerchantOrderStatus(orderId, 50);
        updateMerchantOrderStatus(orderId, 60);

        mockMvc.perform(get("/api/app/auth/profile-stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.orderCount").isNumber())
                .andExpect(jsonPath("$.data.totalSpent").isNumber())
                .andExpect(jsonPath("$.data.rewardPoints").isNumber());
    }

    @Test
    void listDishesReturnsSuccess() throws Exception {
        mockMvc.perform(get("/api/app/dishes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].name").value("热销"));
    }

    @Test
    void listAddressesReturnsDefaultAddress() throws Exception {
        mockMvc.perform(get("/api/app/addresses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].receiverName").value("张三"))
                .andExpect(jsonPath("$.data[0].isDefault").value(true));
    }

    @Test
    void addCartItemReturnsCartSnapshot() throws Exception {
        clearCart();

        Long cartItemId = addCartItem(101, 2);

        mockMvc.perform(get("/api/app/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.items[0].id").value(cartItemId))
                .andExpect(jsonPath("$.data.items[0].dishName").value("玛格丽特披萨"))
                .andExpect(jsonPath("$.data.goodsAmount").value(78.00));
    }

    @Test
    void createOrderForSecondShopReturnsSuccess() throws Exception {
        clearCart();
        Long ramenItemId = addCartItem(2, 201, 1);
        String body = """
                {
                  "shopId": 2,
                  "addressId": 1,
                  "cartItemIds": [%d],
                  "remark": "少辣"
                }
                """.formatted(ramenItemId);

        mockMvc.perform(post("/api/app/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.orderStatus").value(10))
                .andExpect(jsonPath("$.data.payAmount").value(29.00));
    }

    @Test
    void createOrderReturnsSuccess() throws Exception {
        clearCart();
        Long pizzaItemId = addCartItem(101, 1);
        Long snackItemId = addCartItem(103, 1);
        String body = """
                {
                  "shopId": 1,
                  "addressId": 1,
                  "cartItemIds": [%d, %d],
                  "remark": "不要辣"
                }
                """.formatted(pizzaItemId, snackItemId);

        mockMvc.perform(post("/api/app/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.orderStatus").value(10))
                .andExpect(jsonPath("$.data.payAmount").value(65.00));
    }

    @Test
    void createdOrderCanBeListed() throws Exception {
        clearCart();
        Long cartItemId = addCartItem(101, 1);
        String body = """
                {
                  "shopId": 1,
                  "addressId": 1,
                  "cartItemIds": [%d],
                  "remark": ""
                }
                """.formatted(cartItemId);

        mockMvc.perform(post("/api/app/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/app/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").isNumber())
                .andExpect(jsonPath("$.data.records[0].shopName").value("玛利亚披萨"));
    }

    @Test
    void mockPayUpdatesOrderStatus() throws Exception {
        Long orderId = createTestOrder();

        mockMvc.perform(post("/api/app/orders/{id}/mock-pay", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.payStatus").value(1))
                .andExpect(jsonPath("$.data.orderStatus").value(20));
    }

    @Test
    void cancelOrderUpdatesOrderStatus() throws Exception {
        Long orderId = createTestOrder();

        mockMvc.perform(post("/api/app/orders/{id}/cancel", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.orderStatus").value(70));
    }

    @Test
    void merchantCanAcceptAndCompletePaidOrder() throws Exception {
        Long orderId = createPaidOrder();

        mockMvc.perform(post("/api/merchant/orders/{id}/accept", orderId)
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.orderStatus").value(30));

        updateMerchantOrderStatus(orderId, 40);
        updateMerchantOrderStatus(orderId, 50);

        mockMvc.perform(post("/api/merchant/orders/{id}/status", orderId)
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": 60
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.orderStatus").value(60));
    }

    @Test
    void completeOrderLifecycleWorksAcrossAppAndMerchantApis() throws Exception {
        Long orderId = createPaidOrder();
        mockMvc.perform(post("/api/merchant/orders/{id}/accept", orderId).header("Authorization", bearerToken()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.orderStatus").value(30));
        updateMerchantOrderStatus(orderId, 40);
        updateMerchantOrderStatus(orderId, 50);
        mockMvc.perform(post("/api/app/orders/{id}/complete", orderId))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.orderStatus").value(60));
        mockMvc.perform(post("/api/app/orders/{id}/review", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":5,\"content\":\"完整流程评价\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.rating").value(5));
        mockMvc.perform(get("/api/merchant/orders/{id}", orderId).header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderStatus").value(60))
                .andExpect(jsonPath("$.data.review.content").value("完整流程评价"));
    }

    @Test
    void appCanCompleteDeliveringOrder() throws Exception {
        Long orderId = createPaidOrder();

        mockMvc.perform(post("/api/merchant/orders/{id}/accept", orderId)
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        updateMerchantOrderStatus(orderId, 40);
        updateMerchantOrderStatus(orderId, 50);

        mockMvc.perform(post("/api/app/orders/{id}/complete", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.orderStatus").value(60));
    }

    @Test
    void merchantCanRejectPaidOrder() throws Exception {
        Long orderId = createPaidOrder();

        mockMvc.perform(post("/api/merchant/orders/{id}/reject", orderId)
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reason": "商品已售罄"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.orderStatus").value(90))
                .andExpect(jsonPath("$.data.payStatus").value(2))
                .andExpect(jsonPath("$.data.remark").value("商家拒单：商品已售罄"));
    }

    @Test
    void completedOrderCanBeReviewedOnce() throws Exception {
        Long orderId = createPaidOrder();
        mockMvc.perform(post("/api/merchant/orders/{id}/accept", orderId).header("Authorization", bearerToken()))
                .andExpect(status().isOk());
        updateMerchantOrderStatus(orderId, 40);
        updateMerchantOrderStatus(orderId, 50);
        mockMvc.perform(post("/api/app/orders/{id}/complete", orderId)).andExpect(status().isOk());

        String reviewBody = """
                { "rating": 5, "content": "配送很快" }
                """;
        mockMvc.perform(post("/api/app/orders/{id}/review", orderId)
                        .contentType(MediaType.APPLICATION_JSON).content(reviewBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.rating").value(5));
        mockMvc.perform(get("/api/app/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.review.content").value("配送很快"));
        mockMvc.perform(post("/api/app/orders/{id}/review", orderId)
                        .contentType(MediaType.APPLICATION_JSON).content(reviewBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40900));
    }

    @Test
    void unfinishedOrderCannotBeReviewed() throws Exception {
        Long orderId = createTestOrder();
        mockMvc.perform(post("/api/app/orders/{id}/review", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"rating\": 5, \"content\": \"过早评价\" }"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40900));
    }

    @Test
    void usersCannotReadEachOthersOrders() throws Exception {
        Long orderId = createTestOrder();
        mockMvc.perform(get("/api/app/orders/{id}", orderId)
                        .header("Authorization", appBearerToken(2L, "李四")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40400));
    }

    @Test
    void developmentLoginReturnsDistinctUserTokens() throws Exception {
        mockMvc.perform(post("/api/app/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"code\": \"dev-user-2\" }"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(2))
                .andExpect(jsonPath("$.data.token").isNotEmpty());
    }

    @Test
    void developmentLoginCreatesPreviouslyUnknownIdentity() throws Exception {
        try {
            mockMvc.perform(post("/api/app/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"code\":\"new-dev-openid\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.userId").isNumber())
                    .andExpect(jsonPath("$.data.nickname").value("微信用户"))
                    .andExpect(jsonPath("$.data.token").isNotEmpty());
        } finally {
            appUserRepository.findByOpenId("new-dev-openid").ifPresent(appUserRepository::delete);
        }
    }

    @Test
    void authenticatedUserCanBindPhoneInDevelopmentMode() throws Exception {
        var user = appUserRepository.findById(2L).orElseThrow();
        String originalPhone = user.getPhone();
        try {
            mockMvc.perform(post("/api/app/auth/phone")
                            .header("Authorization", appBearerToken(2L, "李四"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"phoneCode\":\"13700000000\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.userId").value(2))
                    .andExpect(jsonPath("$.data.phone").value("13700000000"));
        } finally {
            user = appUserRepository.findById(2L).orElseThrow();
            user.setPhone(originalPhone);
            appUserRepository.save(user);
        }
    }

    @Test
    void appRefreshTokenRotatesSessionButCannotAccessOrders() throws Exception {
        String refresh=jwtService.createAppUserRefreshToken(1L);
        mockMvc.perform(post("/api/app/auth/refresh").contentType(MediaType.APPLICATION_JSON).content("{\"refreshToken\":\""+refresh+"\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.token").isNotEmpty()).andExpect(jsonPath("$.data.refreshToken").isNotEmpty());
        mockMvc.perform(get("/api/app/orders").header("Authorization","Bearer "+refresh))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(40100));
        mockMvc.perform(post("/api/app/auth/refresh").contentType(MediaType.APPLICATION_JSON).content("{\"refreshToken\":\""+refresh+"\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(40100));
    }

    @Test
    void appLogoutRevokesAccessAndRefreshTokens() throws Exception {
        String access = jwtService.createAppUserToken(1L, "张三");
        String refresh = jwtService.createAppUserRefreshToken(1L);
        String body = "{\"refreshToken\":\"" + refresh + "\"}";
        mockMvc.perform(post("/api/app/auth/logout").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0));
        mockMvc.perform(get("/api/app/orders").header("Authorization", "Bearer " + access))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(40100));
        mockMvc.perform(post("/api/app/auth/refresh").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(40100));
    }

    @Test
    void orderCreationIsIdempotentAndDeductsStockOnce() throws Exception {
        clearCart();
        int stockBefore = dishRepository.findById(101L).orElseThrow().getStock();
        Long cartItemId = addCartItem(101, 1);
        String body = """
                { "shopId": 1, "addressId": 1, "cartItemIds": [%d], "remark": "", "idempotencyKey": "test-order-idempotency" }
                """.formatted(cartItemId);
        String first = mockMvc.perform(post("/api/app/orders").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0)).andReturn().getResponse().getContentAsString();
        String second = mockMvc.perform(post("/api/app/orders").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0)).andReturn().getResponse().getContentAsString();
        long firstId = objectMapper.readTree(first).path("data").path("orderId").asLong();
        long secondId = objectMapper.readTree(second).path("data").path("orderId").asLong();
        org.junit.jupiter.api.Assertions.assertEquals(firstId, secondId);
        org.junit.jupiter.api.Assertions.assertEquals(stockBefore - 1, dishRepository.findById(101L).orElseThrow().getStock());
    }

    @Test
    void paymentRequestIsIdempotent() throws Exception {
        Long orderId = createTestOrder();
        long transactionCount = paymentTransactionRepository.count();
        String body = "{ \"requestId\": \"test-payment-idempotency-" + orderId + "\" }";
        mockMvc.perform(post("/api/app/orders/{id}/mock-pay", orderId).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.orderStatus").value(20));
        mockMvc.perform(post("/api/app/orders/{id}/mock-pay", orderId).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.orderStatus").value(20));
        org.junit.jupiter.api.Assertions.assertEquals(transactionCount + 1, paymentTransactionRepository.count());
    }

    @Test
    void cancelAndExpirationRestoreStock() throws Exception {
        int stockBefore = dishRepository.findById(101L).orElseThrow().getStock();
        Long cancelledOrderId = createTestOrder();
        mockMvc.perform(post("/api/app/orders/{id}/cancel", cancelledOrderId)).andExpect(status().isOk());
        org.junit.jupiter.api.Assertions.assertEquals(stockBefore, dishRepository.findById(101L).orElseThrow().getStock());

        Long expiredOrderId = createTestOrder();
        var expiredOrder = orderRepository.findById(expiredOrderId).orElseThrow();
        expiredOrder.setCreatedAt(LocalDateTime.now().minusMinutes(30));
        orderRepository.save(expiredOrder);
        orderExpirationService.closeExpiredOrders();
        org.junit.jupiter.api.Assertions.assertEquals(OrderStatus.CANCELLED.getCode(), orderRepository.findById(expiredOrderId).orElseThrow().getOrderStatus());
        org.junit.jupiter.api.Assertions.assertEquals(stockBefore, dishRepository.findById(101L).orElseThrow().getStock());
    }

    @Test
    void platformCanCancelUnpaidAndRefundPaidOrders() throws Exception {
        int stockBefore=dishRepository.findById(101L).orElseThrow().getStock();
        Long unpaid=createTestOrder();
        mockMvc.perform(post("/api/platform/orders/{id}/cancel",unpaid).header("Authorization",platformBearerToken()).contentType(MediaType.APPLICATION_JSON).content("{\"reason\":\"异常测试订单\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.orderStatus").value(70));
        org.junit.jupiter.api.Assertions.assertEquals(stockBefore,dishRepository.findById(101L).orElseThrow().getStock());
        Long paid=createPaidOrder();
        mockMvc.perform(post("/api/platform/orders/{id}/cancel",paid).header("Authorization",platformBearerToken()).contentType(MediaType.APPLICATION_JSON).content("{\"reason\":\"异常退款\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.orderStatus").value(90)).andExpect(jsonPath("$.data.payStatus").value(2));
    }

    @Test
    void paidCancellationCreatesOneRefundAndRestoresStockOnce() throws Exception {
        int stockBefore = dishRepository.findById(101L).orElseThrow().getStock();
        Long orderId = createPaidOrder();
        long refundsBefore = refundRepository.count();
        String paymentStatusBefore = paymentTransactionRepository.findFirstByOrderIdAndStatusOrderByIdDesc(orderId, "SUCCESS").orElseThrow().getStatus();
        org.junit.jupiter.api.Assertions.assertEquals("SUCCESS", paymentStatusBefore);

        mockMvc.perform(post("/api/app/orders/{id}/cancel", orderId))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.orderStatus").value(90)).andExpect(jsonPath("$.data.payStatus").value(2))
                .andExpect(jsonPath("$.data.refund.refundNo").isNotEmpty()).andExpect(jsonPath("$.data.refund.status").value("SUCCESS"));
        mockMvc.perform(post("/api/app/orders/{id}/cancel", orderId))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.orderStatus").value(90));

        org.junit.jupiter.api.Assertions.assertEquals(refundsBefore + 1, refundRepository.count());
        org.junit.jupiter.api.Assertions.assertEquals(stockBefore, dishRepository.findById(101L).orElseThrow().getStock());
        org.junit.jupiter.api.Assertions.assertTrue(paymentTransactionRepository.findFirstByOrderIdAndStatusOrderByIdDesc(orderId, "REFUNDED").isPresent());
    }

    private Long createTestOrder() throws Exception {
        clearCart();
        Long cartItemId = addCartItem(101, 1);
        String body = """
                {
                  "shopId": 1,
                  "addressId": 1,
                  "cartItemIds": [%d],
                  "remark": ""
                }
                """.formatted(cartItemId);

        String content = mockMvc.perform(post("/api/app/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode root = objectMapper.readTree(content);
        return root.path("data").path("orderId").asLong();
    }

    private Long createPaidOrder() throws Exception {
        Long orderId = createTestOrder();
        mockMvc.perform(post("/api/app/orders/{id}/mock-pay", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.orderStatus").value(20));
        return orderId;
    }

    private void updateMerchantOrderStatus(Long orderId, int statusCode) throws Exception {
        String body = """
                {
                  "status": %d
                }
                """.formatted(statusCode);
        mockMvc.perform(post("/api/merchant/orders/{id}/status", orderId)
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.orderStatus").value(statusCode));
    }

    private Long addCartItem(long dishId, int quantity) throws Exception {
        return addCartItem(1, dishId, quantity);
    }

    private String bearerToken() {
        return "Bearer " + jwtService.createAdminToken(1L, "admin", "MERCHANT_ADMIN");
    }

    private String appBearerToken(Long userId, String nickname) {
        return "Bearer " + jwtService.createAppUserToken(userId, nickname);
    }

    private String platformBearerToken(){return "Bearer "+jwtService.createAdminToken(3L,"platform","PLATFORM_ADMIN",null);}

    private Long addCartItem(long shopId, long dishId, int quantity) throws Exception {
        String body = """
                {
                  "shopId": %d,
                  "dishId": %d,
                  "quantity": %d,
                  "size": "普通",
                  "spice": "不辣",
                  "notes": ""
                }
                """.formatted(shopId, dishId, quantity);

        String content = mockMvc.perform(post("/api/app/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode root = objectMapper.readTree(content);
        return root.path("data").path("id").asLong();
    }

    private void clearCart() throws Exception {
        mockMvc.perform(delete("/api/app/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }
}
