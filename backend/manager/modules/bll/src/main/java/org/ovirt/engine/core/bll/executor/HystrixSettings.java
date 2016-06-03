package org.ovirt.engine.core.bll.executor;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;

public class HystrixSettings {

    public static HystrixCommand.Setter setter(final String key) {
        return HystrixCommand.Setter.withGroupKey(
                HystrixCommandGroupKey.Factory.asKey(key)
        ).andCommandKey(
                HystrixCommandKey.Factory.asKey(key)
        ).andCommandPropertiesDefaults(
                HystrixCommandProperties.Setter()
                        .withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.SEMAPHORE)
                        .withExecutionTimeoutEnabled(false)
                        .withCircuitBreakerEnabled(false)
                        .withFallbackEnabled(false)
                        .withMetricsRollingStatisticalWindowInMilliseconds(60000)
                        .withMetricsRollingStatisticalWindowBuckets(60)
                        .withExecutionIsolationSemaphoreMaxConcurrentRequests(200)
        );
    }

}
