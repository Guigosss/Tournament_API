package com.technofuturtic.tournament_api.bll.services.impls;

import com.technofuturtic.tournament_api.bll.exceptions.RateLimitException;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RateLimitServiceTest {
    private final StringRedisTemplate redis = mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    private final ValueOperations<String, String> values = mock(ValueOperations.class);
    private final RateLimitService service = new RateLimitService(redis);

    @Test void firstRequestConsumesOneTokenAndSetsExpiration() {
        when(redis.opsForValue()).thenReturn(values);
        service.checkRateLimit("ip", "/login", 5, 60);
        verify(values).set(eq("rate_limit:ip:/login"), startsWith("4:"));
        verify(redis).expire("rate_limit:ip:/login", Duration.ofSeconds(60));
    }

    @Test void exhaustedBucketRejectsRequestWithoutWriting() {
        when(redis.opsForValue()).thenReturn(values);
        when(values.get("rate_limit:ip:/login")).thenReturn("0:" + System.currentTimeMillis());
        assertThrows(RateLimitException.class, () -> service.checkRateLimit("ip", "/login", 5, 60));
        verify(values, never()).set(anyString(), anyString());
    }

    @Test void expiredBucketIsRefilledBeforeConsumingToken() {
        when(redis.opsForValue()).thenReturn(values);
        when(values.get("rate_limit:ip:/login")).thenReturn("0:" + (System.currentTimeMillis() - 120_000));
        service.checkRateLimit("ip", "/login", 5, 60);
        verify(values).set(eq("rate_limit:ip:/login"), startsWith("4:"));
    }

    @Test void readingRemainingTokensDoesNotConsumeThem() {
        when(redis.opsForValue()).thenReturn(values);
        assertEquals(5, service.getRemainingTokens("ip", "/login", 5, 60));
        when(values.get("rate_limit:ip:/login")).thenReturn("2:" + System.currentTimeMillis());
        assertEquals(2, service.getRemainingTokens("ip", "/login", 5, 60));
        verify(values, never()).set(anyString(), anyString());
    }
}
