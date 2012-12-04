package org.ovirt.engine.core.bll.gluster;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.ovirt.engine.core.bll.AbstractQueryTest;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.gluster.GlusterServersQueryParameters;
import org.ovirt.engine.core.utils.MockConfigRule;
import org.ovirt.engine.core.utils.ssh.SSHClient;

public class GetGlusterServersQueryTest extends AbstractQueryTest<GlusterServersQueryParameters, GetGlusterServersQuery<GlusterServersQueryParameters>> {

    String serverName1 = "testserver1";
    String password = "password";
    Map<String, String> expectedMap = new HashMap<String, String>();
    SSHClient clientMock;
    String serverName2 = "testserver2";
    String fingerprint1 = "31:e2:1b:7e:89:86:99:c3:f7:1e:57:35:fe:9b:5c:31";
    String fingerprint2 = "31:e2:1b:7e:89:86:99:c3:f7:1e:57:35:fe:9b:5c:32";
    private static final int CONNECT_TO_SERVER_TIMEOUT = 20;
    private static final String GLUSTER_PEER_STATUS_CMD = "gluster peer status --xml";
    private static final String outputXml =
            "<cliOutput><peerStatus><peer><uuid>85c42b0d-c2b7-424a-ae72-5174c25da40b</uuid><hostname>testserver1</hostname><connected>1</connected><state>3</state></peer>"
                    +
                    "<peer><uuid>85c42b0d-c2b7-424a-ae72-5174c25da40b</uuid><hostname>testserver2</hostname><connected>1</connected><state>3</state></peer></peerStatus></cliOutput>";

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.ConnectToServerTimeoutInSeconds, CONNECT_TO_SERVER_TIMEOUT),
            mockConfig(ConfigValues.GlusterPeerStatusCommand, GLUSTER_PEER_STATUS_CMD));

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        setupMock();
        setupExpectedFingerPrint();
    }

    private void setupMock() {
        clientMock = mock(SSHClient.class);
        doReturn(clientMock).when(getQuery()).createSSHClient();
    }

    private void setupExpectedFingerPrint() throws Exception {
        doReturn(serverName1).when(getQueryParameters()).getServerName();
        doReturn(password).when(getQueryParameters()).getPassword();
        doReturn(fingerprint1).when(getQueryParameters()).getFingerprint();
        doReturn(clientMock).when(getQuery()).connect(serverName1);
        doReturn(outputXml).when(getQuery()).executeCommand(clientMock);
        doNothing().when(getQuery()).authenticate(clientMock, "root", password);
        doNothing().when(getQuery()).validateFingerprint(clientMock, fingerprint1);

        expectedMap.put(serverName1, fingerprint1);
        expectedMap.put(serverName2, fingerprint1);
        doReturn(expectedMap).when(getQuery()).extractServers(outputXml);
    }

    @Test
    public void testExecuteQueryCommand() {
        getQuery().executeQueryCommand();
        Map<String, String> serverFingerprintMap =
                (Map<String, String>) getQuery().getQueryReturnValue().getReturnValue();

        assertNotNull(serverFingerprintMap);
        assertEquals(expectedMap, serverFingerprintMap);
    }
}
