package org.egov;

import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ZuulSecurityPropertiesTest {

    @Test
    public void test_should_not_forward_proxy_ip_headers() throws IOException {
        Properties properties = loadMainProperties("application.properties");

        assertEquals("false", properties.getProperty("zuul.addProxyHeaders"));

        Set<String> ignoredHeaders = Arrays.stream(properties.getProperty("zuul.ignoredHeaders").split(","))
            .map(String::trim)
            .collect(Collectors.toSet());

        assertTrue(ignoredHeaders.contains("X-Forwarded-For"));
        assertTrue(ignoredHeaders.contains("X-Forwarded-Host"));
        assertTrue(ignoredHeaders.contains("X-Forwarded-Port"));
        assertTrue(ignoredHeaders.contains("X-Forwarded-Proto"));
        assertTrue(ignoredHeaders.contains("X-Forwarded-Prefix"));
        assertTrue(ignoredHeaders.contains("X-Forwarded-Server"));
        assertTrue(ignoredHeaders.contains("X-Real-IP"));
    }

    @Test
    public void test_should_keep_rate_limit_origin_behind_proxy_enabled() throws IOException {
        Properties properties = loadMainProperties("limiter.properties");

        assertEquals("true", properties.getProperty("zuul.ratelimit.enabled"));
        assertEquals("REDIS", properties.getProperty("zuul.ratelimit.repository"));
        assertEquals("true", properties.getProperty("zuul.ratelimit.behind-proxy"));
        assertEquals("origin", properties.getProperty("zuul.ratelimit.policy-list.user-otp[0].type[1]"));
        assertEquals("4", properties.getProperty("zuul.ratelimit.policy-list.user-otp[0].limit"));
        assertEquals("60", properties.getProperty("zuul.ratelimit.policy-list.user-otp[0].refresh-interval"));
    }

    private Properties loadMainProperties(String fileName) throws IOException {
        Properties properties = new Properties();
        try (FileInputStream inputStream = new FileInputStream("src/main/resources/" + fileName)) {
            properties.load(inputStream);
        }
        return properties;
    }
}
