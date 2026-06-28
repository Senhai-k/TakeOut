package com.takeout;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.takeout.security.JwtService;
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
                .andExpect(jsonPath("$.data.token").value("user-1-token"));
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
                .andExpect(jsonPath("$.data.orderStatus").value(70))
                .andExpect(jsonPath("$.data.remark").value("商家拒单：商品已售罄"));
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
