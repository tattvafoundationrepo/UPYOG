package org.egov.user.security;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * Tracks the single active access token per user so that a NEW login can invalidate the PREVIOUS session
 * (fixes CWE-1018 / concurrent-login). This registry only records which token is "current"; the actual
 * invalidation of the old token is performed by the caller against the OAuth {@code TokenStore} (the same
 * store {@code /user/_logout} uses), so the previous session ends across all services.
 *
 * <h3>Multi-replica correctness</h3>
 * When {@code session.registry.redis.enabled=true} the active-token map is stored in the shared Redis
 * (via {@link StringRedisTemplate}) so ALL replicas see one consistent view — a login on pod B correctly
 * invalidates a session that pod A issued. The swap is a single atomic Redis {@code GETSET}, so two
 * concurrent logins can never both believe they are "first". If Redis is disabled or unreachable the
 * service falls back to an in-memory per-instance map (correct only for a single instance), so a Redis
 * hiccup can never block a login.
 *
 * egov-user already requires Redis for its {@code RedisTokenStore}, so the Redis-backed mode is the
 * sensible default here (see {@code session.registry.redis.enabled} in application.properties).
 *
 * NOTE: moved here from bmc-service-v1 (digit.security.SessionRegistryService).
 */
@Service
@Slf4j
public class SessionRegistryService {

    /** Redis key namespace for the per-user active token. */
    private static final String KEY_PREFIX = "bmc:session:active:";

    /** Fallback store used when Redis is disabled/unreachable: userKey -> currently active access token. */
    private final Map<String, String> activeToken = new ConcurrentHashMap<String, String>();

    @Autowired(required = false)
    private StringRedisTemplate redis;

    @Value("${session.registry.redis.enabled:false}")
    private boolean redisEnabled;

    /**
     * How long an active-token entry lives in Redis. Set this &gt;= the platform access-token validity so the
     * enforcement window covers the token's whole life; otherwise a still-valid old token could be forgotten
     * before it expires and a later login would not invalidate it. Default 7 days.
     */
    @Value("${session.registry.ttl-seconds:604800}")
    private long ttlSeconds;

    public static String userKey(String username, String tenantId, String userType) {
        return (username == null ? "" : username.trim().toLowerCase())
                + "|" + (tenantId == null ? "" : tenantId)
                + "|" + (userType == null ? "" : userType);
    }

    /**
     * Records {@code newToken} as the active token for {@code userKey} and returns the token it replaced
     * (or {@code null} if this is the user's first tracked session). The returned token, if non-null and
     * different, should be revoked to terminate the prior session.
     */
    public String swap(String userKey, String newToken) {
        if (userKey == null || newToken == null) return null;

        if (redisEnabled && redis != null) {
            try {
                String rkey = KEY_PREFIX + userKey;
                // GETSET: atomically set the new token and return the previous one (single round-trip, no race).
                String previous = redis.opsForValue().getAndSet(rkey, newToken);
                if (ttlSeconds > 0) {
                    redis.expire(rkey, ttlSeconds, TimeUnit.SECONDS);
                }
                return previous;
            } catch (Exception e) {
                // Never fail a login because Redis hiccupped — degrade to per-instance enforcement.
                log.warn("Session registry: Redis unavailable ({}). Falling back to in-memory (single-instance) "
                        + "enforcement for this login.", e.getMessage());
            }
        }
        return activeToken.put(userKey, newToken);
    }
}
