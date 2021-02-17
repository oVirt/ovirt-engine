package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.bll.utils.EngineSSHClient;
import org.ovirt.engine.core.common.queries.ServerParameters;

public class GetServerSSHPublicKeyQueryTest extends AbstractQueryTest<ServerParameters, GetServerSSHPublicKeyQuery<ServerParameters>> {

    private static final String EXPECTED_PUBLIC_KEY_PEM =
            "ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIMW8cffvC9e6l2cvdzR3GotbZLzDCybybhz8I7lPnrU9";
    private static final String SERVER_NAME = "localhost";

    private EngineSSHClient mockEngineSSHClient;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        mockEngineSSHClient = mock(EngineSSHClient.class);
        doReturn(mockEngineSSHClient).when(getQuery()).getEngineSSHClient();
    }

    @Test
    public void shouldFetchHostPublicKey() {
        when(getQueryParameters().getServer()).thenReturn(SERVER_NAME);
        doReturn(EXPECTED_PUBLIC_KEY_PEM).when(mockEngineSSHClient).getHostPublicKey();
        getQuery().executeQueryCommand();
        String serverPublicKey = getQuery().getQueryReturnValue().getReturnValue();

        assertNotNull(serverPublicKey);
        assertEquals(EXPECTED_PUBLIC_KEY_PEM, serverPublicKey);
    }

    @Test
    public void shouldReturnNullPublicKeyWhenSSHClientUnableToConnect() throws Exception {
        doThrow(new RuntimeException("ssh client error")).when(mockEngineSSHClient).connect();
        getQuery().executeQueryCommand();

        String serverPublicKey = getQuery().getQueryReturnValue().getReturnValue();

        assertNull(serverPublicKey);
    }

    @Test
    public void shouldReturnNullPublicKeyFromSSHClient() {
        getQuery().executeQueryCommand();
        String serverPublicKey = getQuery().getQueryReturnValue().getReturnValue();

        assertNull(serverPublicKey);
    }

}
