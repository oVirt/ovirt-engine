package org.ovirt.engine.core.bll;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.ovirt.engine.core.common.queries.ServerParameters;
import org.ovirt.engine.core.utils.ssh.EngineSSHDialog;

public class GetServerSSHKeyFingerprintQueryTest extends AbstractQueryTest<ServerParameters, GetServerSSHKeyFingerprintQuery<ServerParameters>> {

    String serverName = "localhost";
    String fingerPrint = "b5:ad:16:19:06:9f:b3:41:69:eb:1c:42:1d:12:b5:31";

    EngineSSHDialog mockEngineSSHDialog;

    private void setupMock() throws Exception {
        mockEngineSSHDialog = mock(EngineSSHDialog.class);
        doNothing().when(mockEngineSSHDialog).connect();
        doNothing().when(mockEngineSSHDialog).authenticate();
        doReturn(mockEngineSSHDialog).when(getQuery()).getEngineSSHDialog();
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        setupMock();
    }

    @Test
    public void testExecuteQueryCommnad() throws IOException{
        when(getQueryParameters().getServer()).thenReturn(serverName);
        doReturn(fingerPrint).when(mockEngineSSHDialog).getHostFingerprint();
        getQuery().executeQueryCommand();
        String serverFingerprint = (String) getQuery().getQueryReturnValue().getReturnValue();

        assertNotNull(serverFingerprint);
        assertEquals(fingerPrint, serverFingerprint);
    }

    @Test
    public void testExecuteQueryCommnadFails() {
        when(getQueryParameters().getServer()).thenReturn(null);
        getQuery().executeQueryCommand();
        String serverFingerprint = (String) getQuery().getQueryReturnValue().getReturnValue();

        assertNull(serverFingerprint);
    }

}
