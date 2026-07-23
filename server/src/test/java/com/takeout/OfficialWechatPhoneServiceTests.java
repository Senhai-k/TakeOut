package com.takeout;

import com.takeout.config.WechatProperties;
import com.takeout.service.OfficialWechatPhoneService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OfficialWechatPhoneServiceTests {
    @Test
    void obtainsAccessTokenAndResolvesPhoneNumber() {
        WechatProperties properties = new WechatProperties();
        properties.setAppId("test-app-id");
        properties.setAppSecret("test-app-secret");
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(once(), requestTo(org.hamcrest.Matchers.containsString("/cgi-bin/token")))
                .andRespond(withSuccess("{\"access_token\":\"access-token\",\"expires_in\":7200}", MediaType.APPLICATION_JSON));
        server.expect(once(), requestTo(org.hamcrest.Matchers.containsString("/wxa/business/getuserphonenumber")))
                .andRespond(withSuccess("{\"errcode\":0,\"phone_info\":{\"phoneNumber\":\"13700000000\",\"purePhoneNumber\":\"13700000000\",\"countryCode\":\"86\"}}", MediaType.APPLICATION_JSON));

        String phone = new OfficialWechatPhoneService(properties, builder).resolvePhoneNumber("phone-code");

        assertEquals("13700000000", phone);
        server.verify();
    }
}
