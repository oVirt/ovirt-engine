package org.ovirt.engine.core.sso.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.BDDMockito.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.verify;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.sso.api.ClientInfo;
import org.ovirt.engine.core.sso.db.SsoDao;
import org.ovirt.engine.core.sso.utils.MissingClientIdCallThrottler;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SsoClientsRegistryTest {

    @Mock
    private SsoDao dao;

    @Mock
    private MissingClientIdCallThrottler throttler;


    private SsoClientsRegistry registry;

    @BeforeEach
    public void setup() {
        registry = new SsoClientsRegistry();
        registry.setThrottler(throttler);
        registry.setSsoDao(dao);

        Map<String, ClientInfo> clientInfos = new HashMap<>() {
            {
                put("testClient1", new ClientInfo().withClientId("testClient1"));
                put("testClient2", new ClientInfo().withClientId("testClient2"));
            }
        };

        given(dao.getAllSsoClientsInfo()).willReturn(clientInfos);
    }

    @Test
    public void shouldInitializedRegistryOnStartup() {
        // given when
        registry.loadRegistry();

        // then
        ClientInfo testClient1 = registry.getClientInfo("testClient1");
        assertNotNull(testClient1);
        assertEquals("testClient1", testClient1.getClientId());
    }

    @Test
    public void shouldAttemptToFetchUnknownClientInfo() {
        // given
        registry.loadRegistry();
        String newClientId = "newClient";
        given(throttler.attemptToMakeACall(newClientId)).willReturn(true);
        given(dao.getSsoClientInfo(newClientId)).willReturn(new ClientInfo().withClientId(newClientId));

        // when
        ClientInfo newClientInfo = registry.getClientInfo(newClientId);

        // then
        assertNotNull(newClientInfo);
        assertEquals(newClientId, newClientInfo.getClientId());
    }

    @Test
    public void shouldReturnNullWhenThrottlingEnabled() {
        // given
        registry.loadRegistry();
        String newClientId = "newClient";
        given(throttler.attemptToMakeACall(newClientId)).willReturn(false);

        // when
        ClientInfo newClientInfo = registry.getClientInfo(newClientId);

        // then
        assertNull(newClientInfo);
        verify(dao, never()).getSsoClientInfo(anyString());
    }

}
