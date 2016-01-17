package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.bll.utils.EngineSSHClient;
import org.ovirt.engine.core.common.queries.ServerParameters;

public class GetServerSSHKeyFingerprintQueryTest extends AbstractQueryTest<ServerParameters, GetServerSSHKeyFingerprintQuery<ServerParameters>> {

    String serverName = "localhost";
    String fingerPrint = "b5:ad:16:19:06:9f:b3:41:69:eb:1c:42:1d:12:b5:31";

    EngineSSHClient mockEngineSSHClient;

    private void setupMock() throws Exception {
        mockEngineSSHClient = mock(EngineSSHClient.class);
        doReturn(mockEngineSSHClient).when(getQuery()).getEngineSSHClient();
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        setupMock();
    }

    @Test
    public void testExecuteQueryCommnad() throws Exception {
        when(getQueryParameters().getServer()).thenReturn(serverName);
        doReturn(fingerPrint).when(mockEngineSSHClient).getHostFingerprint();
        getQuery().executeQueryCommand();
        String serverFingerprint = getQuery().getQueryReturnValue().getReturnValue();

        assertNotNull(serverFingerprint);
        assertEquals(fingerPrint, serverFingerprint);
    }

    @Test
    public void testExecuteQueryCommnadFails() {
        when(getQueryParameters().getServer()).thenReturn(null);
        getQuery().executeQueryCommand();
        String serverFingerprint = getQuery().getQueryReturnValue().getReturnValue();

        assertNull(serverFingerprint);
    }
}
