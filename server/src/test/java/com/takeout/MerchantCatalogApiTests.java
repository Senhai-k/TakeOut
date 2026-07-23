package com.takeout;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import com.takeout.security.JwtService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MerchantCatalogApiTests extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Test
    void adminLoginReturnsSession() throws Exception {
        mockMvc.perform(post("/api/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "admin",
                                  "password": "123456"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.role").value("MERCHANT_ADMIN"))
                .andExpect(jsonPath("$.data.token").isNotEmpty());
    }

    @Test
    void adminRefreshTokenRotatesSessionButCannotAccessMerchantApi() throws Exception {
        String refresh=jwtService.createAdminRefreshToken(1L,"admin","MERCHANT_ADMIN");
        mockMvc.perform(post("/api/admin/auth/refresh").contentType(MediaType.APPLICATION_JSON).content("{\"refreshToken\":\""+refresh+"\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.token").isNotEmpty()).andExpect(jsonPath("$.data.refreshToken").isNotEmpty());
        mockMvc.perform(get("/api/merchant/categories").header("Authorization","Bearer "+refresh))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(40100));
        mockMvc.perform(post("/api/admin/auth/refresh").contentType(MediaType.APPLICATION_JSON).content("{\"refreshToken\":\""+refresh+"\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(40100));
    }

    @Test
    void adminLogoutRevokesAccessAndRefreshTokens() throws Exception {
        String access = jwtService.createAdminToken(1L, "admin", "MERCHANT_ADMIN", 1L);
        String refresh = jwtService.createAdminRefreshToken(1L, "admin", "MERCHANT_ADMIN");
        String body = "{\"refreshToken\":\"" + refresh + "\"}";
        mockMvc.perform(post("/api/admin/auth/logout").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0));
        mockMvc.perform(get("/api/merchant/categories").header("Authorization", "Bearer " + access))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(40100));
        mockMvc.perform(post("/api/admin/auth/refresh").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(40100));
    }

    @Test
    void adminLoginRejectsInvalidPassword() throws Exception {
        mockMvc.perform(post("/api/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "admin",
                                  "password": "wrong"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40100));
    }

    @Test
    void malformedJsonReturnsStableParameterErrorWithoutFrameworkDetails() throws Exception {
        mockMvc.perform(post("/api/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not-json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40001))
                .andExpect(jsonPath("$.message").value("请求体格式错误"));
    }

    @Test
    void repeatedLoginFailuresTemporarilyLockAccountAndPasswordResetUnlocksIt() throws Exception {
        String wrongPassword = "{\"username\":\"merchant2\",\"password\":\"wrong\"}";
        for (int attempt = 0; attempt < 5; attempt++) {
            mockMvc.perform(post("/api/admin/auth/login").contentType(MediaType.APPLICATION_JSON).content(wrongPassword))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(40100));
        }
        String correctPassword = "{\"username\":\"merchant2\",\"password\":\"123456\"}";
        mockMvc.perform(post("/api/admin/auth/login").contentType(MediaType.APPLICATION_JSON).content(correctPassword))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(40100));

        mockMvc.perform(post("/api/platform/merchant-admins/2/password")
                        .header("Authorization", platformToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"123456\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0));
        mockMvc.perform(post("/api/admin/auth/login").contentType(MediaType.APPLICATION_JSON).content(correctPassword))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.token").isNotEmpty());
    }

    @Test
    void listCategoriesReturnsSuccess() throws Exception {
        mockMvc.perform(get("/api/merchant/categories")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    void merchantApiRequiresAdminToken() throws Exception {
        mockMvc.perform(get("/api/merchant/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40100));
    }

    @Test
    void merchantApiRejectsAppUserToken() throws Exception {
        mockMvc.perform(get("/api/merchant/categories")
                        .header("Authorization", "Bearer " + jwtService.createAppUserToken(1L, "张三")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40100));
    }

    @Test
    void secondMerchantOnlySeesOwnCatalogAndShop() throws Exception {
        String token = merchantToken(2L, "merchant2", 2L);
        mockMvc.perform(get("/api/merchant/categories").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0].shopId").value(2));
        mockMvc.perform(get("/api/merchant/shop").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(2));
    }

    @Test
    void secondMerchantCannotModifyFirstMerchantsCategory() throws Exception {
        mockMvc.perform(put("/api/merchant/categories/101")
                        .header("Authorization", merchantToken(2L, "merchant2", 2L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"name\": \"越权修改\", \"sort\": 1, \"status\": 1 }"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40400));
    }

    @Test
    void platformAdminCannotCallMerchantApiWithoutShopContext() throws Exception {
        String token = "Bearer " + jwtService.createAdminToken(3L, "platform", "PLATFORM_ADMIN", null);
        mockMvc.perform(get("/api/merchant/categories").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40100));
    }

    @Test
    void secondMerchantLoginReturnsShopIdentity() throws Exception {
        mockMvc.perform(post("/api/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"username\": \"merchant2\", \"password\": \"123456\" }"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.shopId").value(2))
                .andExpect(jsonPath("$.data.role").value("MERCHANT_ADMIN"));
    }

    @Test
    void platformAdminCanManageUsersAndShops() throws Exception {
        String token = platformToken();
        mockMvc.perform(get("/api/platform/overview").header("Authorization", token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.userCount").value(2)).andExpect(jsonPath("$.data.shopCount").value(org.hamcrest.Matchers.greaterThanOrEqualTo(3)));
        mockMvc.perform(get("/api/platform/users?page=1&pageSize=10").header("Authorization", token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.records").isArray());
        mockMvc.perform(post("/api/platform/users/2/status").header("Authorization", token).contentType(MediaType.APPLICATION_JSON).content("{ \"status\": 0 }"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.status").value(0));
        mockMvc.perform(post("/api/platform/users/2/status").header("Authorization", token).contentType(MediaType.APPLICATION_JSON).content("{ \"status\": 1 }"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/platform/shops/3/status").header("Authorization", token).contentType(MediaType.APPLICATION_JSON).content("{ \"status\": 0 }"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.status").value(0));
        mockMvc.perform(post("/api/platform/shops/3/status").header("Authorization", token).contentType(MediaType.APPLICATION_JSON).content("{ \"status\": 1 }"))
                .andExpect(status().isOk());
    }

    @Test
    void merchantAdminCannotCallPlatformApi() throws Exception {
        mockMvc.perform(get("/api/platform/overview").header("Authorization", bearerToken()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(40100));
    }

    @Test
    void platformCanCreateShopWithMerchantAccount() throws Exception {
        mockMvc.perform(post("/api/platform/shops").header("Authorization", platformToken()).contentType(MediaType.APPLICATION_JSON).content("""
                {"name":"测试新店","phone":"13700000000","address":"测试地址","minOrderAmount":10,"deliveryFee":2,"businessStatus":1,"status":1,"adminUsername":"newmerchant","adminPassword":"123456"}
                """))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0)).andExpect(jsonPath("$.data.name").value("测试新店"));
    }

    @Test
    void platformCanReadOrderDetailAndConfigAudit() throws Exception {
        String token = platformToken();
        mockMvc.perform(get("/api/platform/configs").header("Authorization", token)).andExpect(status().isOk()).andExpect(jsonPath("$.data").isArray());
        mockMvc.perform(put("/api/platform/configs/order.unpaid_timeout_minutes").header("Authorization", token).contentType(MediaType.APPLICATION_JSON).content("{\"value\":\"20\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.value").value("20"));
        mockMvc.perform(get("/api/platform/audit-logs").header("Authorization", token)).andExpect(status().isOk()).andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    void platformCanManageMerchantAdminAccount() throws Exception {
        String token=platformToken();
        mockMvc.perform(get("/api/platform/merchant-admins?shopId=2").header("Authorization",token)).andExpect(status().isOk()).andExpect(jsonPath("$.data.records[0].shopId").value(2));
        mockMvc.perform(post("/api/platform/merchant-admins/2/status").header("Authorization",token).contentType(MediaType.APPLICATION_JSON).content("{\"status\":0}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.status").value(0));
        mockMvc.perform(post("/api/platform/merchant-admins/2/status").header("Authorization",token).contentType(MediaType.APPLICATION_JSON).content("{\"status\":1}"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/platform/merchant-admins/2/password").header("Authorization",token).contentType(MediaType.APPLICATION_JSON).content("{\"password\":\"123456\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/platform/audit-logs?action=RESET_PASSWORD").header("Authorization",token)).andExpect(status().isOk()).andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    void listDishesReturnsSuccess() throws Exception {
        mockMvc.perform(get("/api/merchant/dishes")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    void merchantCanReadAndUpdateShop() throws Exception {
        mockMvc.perform(get("/api/merchant/shop").header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));

        mockMvc.perform(put("/api/merchant/shop")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "TakeOut 测试店",
                                  "logoUrl": "",
                                  "notice": "今日营业",
                                  "phone": "13800000000",
                                  "address": "测试地址",
                                  "minOrderAmount": 18,
                                  "deliveryFee": 3,
                                  "businessStatus": 1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("TakeOut 测试店"))
                .andExpect(jsonPath("$.data.minOrderAmount").value(18));
    }

    @Test
    void createCategoryReturnsSuccess() throws Exception {
        mockMvc.perform(post("/api/merchant/categories")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "测试分类",
                                  "sort": 99,
                                  "status": 1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.name").value("测试分类"));
    }

    @Test
    void uploadImageReturnsPublicUrl() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "dish.png",
                MediaType.IMAGE_PNG_VALUE,
                new byte[]{(byte) 0x89, 'P', 'N', 'G', 0x0d, 0x0a, 0x1a, 0x0a, 0, 0, 0, 0}
        );

        mockMvc.perform(multipart("/api/merchant/uploads/images")
                        .file(file)
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.url").value(org.hamcrest.Matchers.startsWith("/uploads/")));
    }

    @Test
    void uploadImageRejectsUnsupportedContentType() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "dish.png",
                MediaType.TEXT_PLAIN_VALUE,
                new byte[]{1, 2, 3}
        );

        mockMvc.perform(multipart("/api/merchant/uploads/images")
                        .file(file)
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40001));
    }

    @Test
    void uploadImageRejectsContentThatDoesNotMatchDeclaredType() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "dish.png", MediaType.IMAGE_PNG_VALUE, new byte[]{1, 2, 3, 4});
        mockMvc.perform(multipart("/api/merchant/uploads/images").file(file).header("Authorization", bearerToken()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(40001));
    }

    @Test
    void uploadImageRejectsOversizedFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "large.png",
                MediaType.IMAGE_PNG_VALUE,
                new byte[(2 * 1024 * 1024) + 1]
        );

        mockMvc.perform(multipart("/api/merchant/uploads/images")
                        .file(file)
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40001));
    }

    @Test
    void resetSeedDataReturnsSeedCounts() throws Exception {
        mockMvc.perform(post("/api/platform/seed/reset")
                        .header("Authorization", "Bearer " + jwtService.createAdminToken(3L, "platform", "PLATFORM_ADMIN", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.shops").value(3))
                .andExpect(jsonPath("$.data.categories").value(12))
                .andExpect(jsonPath("$.data.dishes").value(12))
                .andExpect(jsonPath("$.data.orders").value(5));
    }

    private String bearerToken() {
        return "Bearer " + jwtService.createAdminToken(1L, "admin", "MERCHANT_ADMIN");
    }

    private String merchantToken(Long adminId, String username, Long shopId) {
        return "Bearer " + jwtService.createAdminToken(adminId, username, "MERCHANT_ADMIN", shopId);
    }

    private String platformToken() {
        return "Bearer " + jwtService.createAdminToken(3L, "platform", "PLATFORM_ADMIN", null);
    }
}
