package com.takeout;

import com.takeout.config.WechatProperties;
import com.takeout.exception.BusinessException;
import com.takeout.service.OfficialWechatIdentityService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OfficialWechatIdentityServiceTests {
    @Test
    void exchangesCodeForWechatIdentity() {
        WechatProperties properties = properties();
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(once(), requestTo(org.hamcrest.Matchers.containsString("/sns/jscode2session")))
                .andRespond(withSuccess("{\"openid\":\"wx-openid\",\"unionid\":\"wx-unionid\"}", MediaType.APPLICATION_JSON));

        var identity = new OfficialWechatIdentityService(properties, builder).exchangeCode("login-code");

        assertEquals("wx-openid", identity.openId());
        assertEquals("wx-unionid", identity.unionId());
        server.verify();
    }

    @Test
    void rejectsWechatErrorResponse() {
        WechatProperties properties = properties();
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(once(), requestTo(org.hamcrest.Matchers.containsString("/sns/jscode2session")))
                .andRespond(withSuccess("{\"errcode\":40029,\"errmsg\":\"invalid code\"}", MediaType.APPLICATION_JSON));

        assertThrows(BusinessException.class,
                () -> new OfficialWechatIdentityService(properties, builder).exchangeCode("invalid-code"));
        server.verify();
    }

    private WechatProperties properties() {
        WechatProperties properties = new WechatProperties();
        properties.setAppId("test-app-id");
        properties.setAppSecret("test-app-secret");
        return properties;
    }
}
