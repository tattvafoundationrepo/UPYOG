package org.egov.Utils;

import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.Assert.assertEquals;

public class CustomRateLimitUtilsTest {

    @Test
    public void test_should_use_first_forwarded_for_ip_when_behind_proxy() {
        RateLimitProperties properties = new RateLimitProperties();
        properties.setBehindProxy(true);
        CustomRateLimitUtils rateLimitUtils = new CustomRateLimitUtils(properties);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.244.1.20");
        request.addHeader("X-Forwarded-For", "27.60.40.75, 10.0.0.10");

        assertEquals("27.60.40.75", rateLimitUtils.getRemoteAddress(request));
    }

    @Test
    public void test_should_fallback_to_remote_addr_when_forwarded_for_missing() {
        RateLimitProperties properties = new RateLimitProperties();
        properties.setBehindProxy(true);
        CustomRateLimitUtils rateLimitUtils = new CustomRateLimitUtils(properties);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.244.1.20");

        assertEquals("10.244.1.20", rateLimitUtils.getRemoteAddress(request));
    }
}
