package com.takeout;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.takeout.config.RateLimitProperties;
import com.takeout.security.ApiRateLimitFilter;
import com.takeout.security.InMemoryRateLimiter;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiRateLimitFilterTests {

    @Test
    void rejectsAuthenticationRequestsOverTheConfiguredLimit() throws Exception {
        RateLimitProperties properties = new RateLimitProperties();
        properties.setAuthMaxRequests(2);
        ApiRateLimitFilter filter = new ApiRateLimitFilter(properties, new ObjectMapper(), new InMemoryRateLimiter());

        assertEquals(200, execute(filter, "10.0.0.1", null).getStatus());
        assertEquals(200, execute(filter, "10.0.0.1", null).getStatus());
        MockHttpServletResponse blocked = execute(filter, "10.0.0.1", null);

        assertEquals(429, blocked.getStatus());
        assertEquals(42900, new ObjectMapper().readTree(blocked.getContentAsString()).path("code").asInt());
        assertEquals("60", blocked.getHeader("Retry-After"));
        assertEquals(200, execute(filter, "10.0.0.2", null).getStatus());
    }

    @Test
    void forwardedAddressIsOnlyUsedWhenExplicitlyTrusted() throws Exception {
        RateLimitProperties properties = new RateLimitProperties();
        properties.setAuthMaxRequests(1);
        ApiRateLimitFilter filter = new ApiRateLimitFilter(properties, new ObjectMapper(), new InMemoryRateLimiter());

        assertEquals(200, execute(filter, "127.0.0.1", "10.0.0.1").getStatus());
        assertEquals(429, execute(filter, "127.0.0.1", "10.0.0.2").getStatus());

        properties.setTrustForwardedHeaders(true);
        ApiRateLimitFilter trustedFilter = new ApiRateLimitFilter(properties, new ObjectMapper(), new InMemoryRateLimiter());
        assertEquals(200, execute(trustedFilter, "127.0.0.1", "10.0.0.1").getStatus());
        assertEquals(200, execute(trustedFilter, "127.0.0.1", "10.0.0.2").getStatus());
    }

    private MockHttpServletResponse execute(ApiRateLimitFilter filter, String remoteAddress, String forwardedAddress) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/admin/auth/login");
        request.setRemoteAddr(remoteAddress);
        if (forwardedAddress != null) request.addHeader("X-Forwarded-For", forwardedAddress);
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response;
    }
}
