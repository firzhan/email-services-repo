package com.siteminder.email.utils;

import com.siteminder.email.exception.EmailClientNotAvailableException;
import com.siteminder.email.model.request.EmailAddress;
import com.siteminder.email.model.request.InboundEmailMsg;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public final class TestUtils {
    private TestUtils() {}

    public static InboundEmailMsg dummyEmailMessage() {
       /* EmailAddress from = new EmailAddress("testuser1@mailgun.com", "test1");
        List<EmailAddress> fromList = new ArrayList<>();
        fromList.add(from);*/
        EmailAddress to = new EmailAddress("testuser2@sendgrid.com", "test2");
        List<EmailAddress> toList = new ArrayList<>();
        toList.add(to);

        InboundEmailMsg inboundEmailMsg = new InboundEmailMsg();
        inboundEmailMsg.setTo(toList);
        inboundEmailMsg.setSubject("sub1");
        inboundEmailMsg.setContent("con1");

        return inboundEmailMsg;
    }

    public static CircuitBreakerRegistry defaultCircuitBreakerRegistry() {
        return CircuitBreakerRegistry.of(
                CircuitBreakerConfig.custom()
                        .failureRateThreshold(50)
                        .waitDurationInOpenState(Duration.ofMillis(2000))
                        .slidingWindow(10,
                        10,
                        CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                        .permittedNumberOfCallsInHalfOpenState(10)
                        .build());
    }

    public static RetryRegistry defaultRetryRegistry() {
        return RetryRegistry.of(
                RetryConfig.custom()
                        .maxAttempts(2)
                        .waitDuration(Duration.ofMillis(500))
                        .retryExceptions(EmailClientNotAvailableException.class)
                        .build());
    }

}
