package com.takeout;

import com.takeout.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MerchantStatisticsApiTests extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Test
    void overviewReturnsSuccess() throws Exception {
        mockMvc.perform(get("/api/merchant/statistics/overview")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.todayOrderCount").exists())
                .andExpect(jsonPath("$.data.todaySalesAmount").exists())
                .andExpect(jsonPath("$.data.pendingOrderCount").exists())
                .andExpect(jsonPath("$.data.dishCount").exists());
    }

    private String bearerToken() {
        return "Bearer " + jwtService.createAdminToken(1L, "admin", "MERCHANT_ADMIN");
    }
}
