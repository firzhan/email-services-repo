package com.siteminder.email.config;

import com.siteminder.email.exception.EmailClientNotAvailableException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class CircuitBreakerServiceConfig {

    @Value("${resilience4j.circuit-breaker.failure-rate-threshold}")
    private float circuitBreakerFailureRateThreshold;

    @Value("${resilience4j.circuit-breaker.duration-in-open-state-ms}")
    private long circuitBreakerDurationInOpenState;

    @Value("${resilience4j.circuit-breaker.ring-buffer-size-half-open-state}")
    private int circuitBreakerRingBufferSizeHalfOpenState;

    @Value("${resilience4j.circuit-breaker.ring-buffer-size-closed-state}")
    private int circuitBreakerRingBufferSizeClosedState;

    @Value("${resilience4j.retry.max-attempts}")
    private int retryMaxAttempts;

    @Value("${resilience4j.retry.wait-duration-ms}")
    private long retryWaitDuration;

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        return CircuitBreakerRegistry.of(defaultCircuitBreakerConfig());
    }

    @Bean
    public RetryRegistry retryRegistry() {
        return RetryRegistry.of(defaultRetryConfig());
    }

    private CircuitBreakerConfig defaultCircuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
                .failureRateThreshold(circuitBreakerFailureRateThreshold)
                .waitDurationInOpenState(Duration.ofMillis(circuitBreakerDurationInOpenState))
                .slidingWindow(circuitBreakerRingBufferSizeClosedState,
                        circuitBreakerRingBufferSizeClosedState,
                        CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .permittedNumberOfCallsInHalfOpenState(circuitBreakerRingBufferSizeHalfOpenState)
                .build();
    }

    private RetryConfig defaultRetryConfig() {
        return RetryConfig.custom()
                .maxAttempts(retryMaxAttempts)
                .waitDuration(Duration.ofMillis(retryWaitDuration))
                .retryExceptions(EmailClientNotAvailableException.class)
                .build();
    }
}
