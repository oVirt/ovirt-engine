package org.ovirt.engine.core.sso.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.ovirt.engine.core.sso.utils.MissingClientIdCallThrottler.DEFAULT_MAX_ALLOWED_MISSING_UNIQUE_CLIENTS;
import static org.ovirt.engine.core.sso.utils.MissingClientIdCallThrottler.DEFAULT_MAX_MISSING_CLIENT_REFRESH_ATTEMPTS_PER_MINUTE;
import static org.ovirt.engine.core.sso.utils.MissingClientIdCallThrottler.SSO_MAX_ALLOWED_MISSING_UNIQUE_CLIENTS;
import static org.ovirt.engine.core.sso.utils.MissingClientIdCallThrottler.SSO_MAX_MISSING_CLIENT_REFRESH_ATTEMPTS_PER_MINUTE;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class MissingClientIdCallThrottlerTest {

    private MissingClientIdCallThrottler throttler;
    private final Instant currentTime = Instant.now().truncatedTo(ChronoUnit.MINUTES).plusSeconds(15);

    private SsoLocalConfig ssoLocalConfig;
    private Clock clock;

    @BeforeEach
    public void setup() {
        throttler = new MissingClientIdCallThrottler();

        ssoLocalConfig = Mockito.mock(SsoLocalConfig.class);
        given(ssoLocalConfig.getInteger(SSO_MAX_MISSING_CLIENT_REFRESH_ATTEMPTS_PER_MINUTE,
                DEFAULT_MAX_MISSING_CLIENT_REFRESH_ATTEMPTS_PER_MINUTE))
                        .willReturn(DEFAULT_MAX_MISSING_CLIENT_REFRESH_ATTEMPTS_PER_MINUTE);
        given(ssoLocalConfig.getInteger(SSO_MAX_ALLOWED_MISSING_UNIQUE_CLIENTS,
                DEFAULT_MAX_ALLOWED_MISSING_UNIQUE_CLIENTS))
                        .willReturn(DEFAULT_MAX_ALLOWED_MISSING_UNIQUE_CLIENTS);
        throttler.setSsoLocalConfig(ssoLocalConfig);

        clock = Mockito.mock(Clock.class);
        given(clock.instant()).willReturn(currentTime);
        throttler.setClockSupplier(() -> clock);
    }

    @Test
    public void shouldAllowFirstCallForAClientInGivenMinute() {
        assertTrue(throttler.attemptToMakeACall("client1"));
    }

    @Test
    public void shouldAllowACallInNewMinutePeriodWhenPreviousOneExceeded() {
        for (int i = 0; i < DEFAULT_MAX_MISSING_CLIENT_REFRESH_ATTEMPTS_PER_MINUTE; i++) {
            throttler.attemptToMakeACall("client1");
        }
        assertFalse(throttler.attemptToMakeACall("client1"));

        given(clock.instant()).willReturn(currentTime.plusSeconds(60));
        assertTrue(throttler.attemptToMakeACall("client1"));
    }

    @Test
    public void shouldClearAllRegisteredCallsFromPreviousMinutePeriod() {
        for (int i = 0; i < DEFAULT_MAX_MISSING_CLIENT_REFRESH_ATTEMPTS_PER_MINUTE; i++) {
            throttler.attemptToMakeACall("client1");
        }
        assertFalse(throttler.attemptToMakeACall("client1"));

        // move to the next time window
        given(clock.instant()).willReturn(currentTime.plusSeconds(60));
        assertTrue(throttler.attemptToMakeACall("client1"));

        // lets get back in time check whether previous minute records has been reset
        given(clock.instant()).willReturn(currentTime.minusSeconds(60));
        assertTrue(throttler.attemptToMakeACall("client1"));

    }

    @Test
    public void shouldBlockClientCallWhenMaxAttemptsAlreadyReached() {
        for (int i = 0; i < DEFAULT_MAX_MISSING_CLIENT_REFRESH_ATTEMPTS_PER_MINUTE; i++) {
            throttler.attemptToMakeACall("client1");
        }
        assertFalse(throttler.attemptToMakeACall("client1"));
    }

    @Test
    public void shouldBlockClientCallWhenConfigurableMaxAttemptsAlreadyReached() {
        int maxAttempts = 3;
        given(ssoLocalConfig.getInteger(SSO_MAX_MISSING_CLIENT_REFRESH_ATTEMPTS_PER_MINUTE,
                DEFAULT_MAX_MISSING_CLIENT_REFRESH_ATTEMPTS_PER_MINUTE))
                        .willReturn(maxAttempts);

        for (int i = 0; i < maxAttempts; i++) {
            throttler.attemptToMakeACall("client1");
        }
        assertFalse(throttler.attemptToMakeACall("client1"));
    }

    @Test
    public void shouldNotAllowClientIdRegistryToGrowInfinitely() {
        int maxUniqueClients = 100;
        given(ssoLocalConfig.getInteger(SSO_MAX_ALLOWED_MISSING_UNIQUE_CLIENTS,
                DEFAULT_MAX_ALLOWED_MISSING_UNIQUE_CLIENTS))
                        .willReturn(maxUniqueClients);

        for (int i = 0; i <= maxUniqueClients * 0.9; i++) {
            assertTrue(throttler.attemptToMakeACall("testClient" + i));
        }

        given(clock.instant()).willReturn(currentTime.plusSeconds(600));

        assertTrue(throttler.attemptToMakeACall("newClient"));

        // now the cleaup should have already been performed
        // lets get back in time to verify it
        given(clock.instant()).willReturn(currentTime.minusSeconds(600));

        // there is already 'newClient' call registered so we need to substract 1 call
        for (int i = 0; i < maxUniqueClients - 1; i++) {
            assertTrue(throttler.attemptToMakeACall("testClient" + i));
        }

        assertFalse(throttler.attemptToMakeACall("someOtherClient"));
    }

}
