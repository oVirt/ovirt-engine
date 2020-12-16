package org.ovirt.engine.core.sso.utils;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class MissingClientIdCallThrottler {

    public static final Logger logger = LoggerFactory.getLogger(MissingClientIdCallThrottler.class);

    // This is a precaution to avoid situation when during a single minute of time hundred of thousands single
    // sso client id calls are sent negatively impacting overall performance
    static final int DEFAULT_MAX_MISSING_CLIENT_REFRESH_ATTEMPTS_PER_MINUTE = 5;
    static final String SSO_MAX_MISSING_CLIENT_REFRESH_ATTEMPTS_PER_MINUTE =
            "SSO_MAX_MISSING_CLIENT_REFRESH_ATTEMPTS_PER_MINUTE";

    // This is a precaution to avoid situation when during a single minute hundred of thousands unique sso client calls
    // are sent negatively impacting overall performance. This could happen for example by malicious use
    // of Rest API client.
    // In real scenario such situation should not take place - client id represents a single and registered sso client
    // that is already known.
    static final int DEFAULT_MAX_ALLOWED_MISSING_UNIQUE_CLIENTS = 2;
    static final String SSO_MAX_ALLOWED_MISSING_UNIQUE_CLIENTS = "SSO_MAX_ALLOWED_MISSING_UNIQUE_CLIENTS";

    private final AtomicInteger clientsCount = new AtomicInteger(0);
    private final ConcurrentMap<String, CallsPerMinute> callsPerMinute = new ConcurrentHashMap<>();
    private Supplier<Clock> clockSupplier = Clock::systemUTC;

    @Inject
    private SsoLocalConfig ssoLocalConfig;

    public boolean attemptToMakeACall(String clientId) {
        Instant currentMinute = clockSupplier.get().instant().truncatedTo(ChronoUnit.MINUTES);

        CallsPerMinute clientIdCallsPerMinute = callsPerMinute.compute(clientId, (_clientId, _callsPerMinute) -> {
            if (_callsPerMinute == null) {
                int maxUniqueClients = getMaxUniqueClients();

                if (clientsCount.get() > maxUniqueClients * 0.9) {
                    logger.debug("About to perform client id calls registry cleanup");
                    cleanOldClientCallRecords(currentMinute);
                }

                if (clientsCount.incrementAndGet() <= maxUniqueClients) {
                    return new CallsPerMinute(currentMinute);
                }

                logger.warn("Client id call {} throttled because max allowed client record count {} exceeded",
                        clientId,
                        maxUniqueClients);
                return null;
            }

            return _callsPerMinute.withNewCall(currentMinute);
        });

        if (clientIdCallsPerMinute == null) {
            return false;
        }

        int maxAllowedAttempts = getMaxAttemptsPerMinute();
        return clientIdCallsPerMinute.count <= maxAllowedAttempts;
    }

    private void cleanOldClientCallRecords(Instant currentMinute) {
        callsPerMinute.forEach((_c, _calls) -> {
            // 120 seconds is an arbitrary choice. IMO it cleans unused data efficiently and leaves the most recent
            // to ease GC with its regular work
            if (_calls.createdAt.plusSeconds(120).isBefore(currentMinute)) {
                callsPerMinute.remove(_c);
                clientsCount.decrementAndGet();
            }
        });
    }

    // for tests
    void setClockSupplier(Supplier<Clock> clockSupplier) {
        this.clockSupplier = clockSupplier;
    }

    // for tests
    void setSsoLocalConfig(SsoLocalConfig ssoLocalConfig) {
        this.ssoLocalConfig = ssoLocalConfig;
    }

    public static class CallsPerMinute {
        private Instant createdAt;
        private int count = 1;

        public CallsPerMinute(Instant createdAt) {
            this.createdAt = createdAt;
        }

        public CallsPerMinute withNewCall(Instant currentMinute) {
            if (currentMinute.isAfter(createdAt)) {
                this.createdAt = currentMinute;
                this.count = 1;
            } else {
                this.count++;
            }

            return this;
        }

    }

    private int getMaxAttemptsPerMinute() {
        return ssoLocalConfig.getInteger(SSO_MAX_MISSING_CLIENT_REFRESH_ATTEMPTS_PER_MINUTE,
                DEFAULT_MAX_MISSING_CLIENT_REFRESH_ATTEMPTS_PER_MINUTE);
    }

    private int getMaxUniqueClients() {
        return ssoLocalConfig.getInteger(SSO_MAX_ALLOWED_MISSING_UNIQUE_CLIENTS,
                DEFAULT_MAX_ALLOWED_MISSING_UNIQUE_CLIENTS);
    }
}
