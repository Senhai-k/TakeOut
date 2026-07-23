package com.takeout;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "takeout.app.allow-anonymous-test-user=false")
@AutoConfigureMockMvc
class UserAuthenticationApiTests extends BaseIntegrationTest {
    @Autowired private MockMvc mockMvc;

    @Test
    void userDataApiRequiresToken() throws Exception {
        mockMvc.perform(get("/api/app/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40100));
    }

    @Test
    void phoneBindingRequiresToken() throws Exception {
        mockMvc.perform(post("/api/app/auth/phone")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phoneCode\":\"13700000000\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40100));
    }
}
