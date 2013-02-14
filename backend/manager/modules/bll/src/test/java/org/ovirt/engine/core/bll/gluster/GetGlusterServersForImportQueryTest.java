package org.ovirt.engine.core.bll.gluster;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.ovirt.engine.core.bll.AbstractQueryTest;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.gluster.GlusterServersQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsStaticDAO;
import org.ovirt.engine.core.utils.MockConfigRule;
import org.ovirt.engine.core.utils.ssh.SSHClient;

public class GetGlusterServersForImportQueryTest extends AbstractQueryTest<GlusterServersQueryParameters, GetGlusterServersForImportQuery<GlusterServersQueryParameters>> {

    private static final String SERVER_NAME1 = "testserver1";
    private static final String SERVER_NAME2 = "testserver2";
    private static final String NEW_SERVER = "testserver3";
    private static final String PASSWORD = "password";
    private Map<String, String> EXPECTED_MAP = new HashMap<String, String>();
    private static final String FINGER_PRINT1 = "31:e2:1b:7e:89:86:99:c3:f7:1e:57:35:fe:9b:5c:31";
    private static final int CONNECT_TO_SERVER_TIMEOUT = 20;
    private static final String GLUSTER_PEER_STATUS_CMD = "gluster peer status --xml";
    private static final String OUTPUT_XML =
            "<cliOutput><peerStatus><peer><uuid>85c42b0d-c2b7-424a-ae72-5174c25da40b</uuid><hostname>testserver1</hostname><connected>1</connected><state>3</state></peer>"
                    +
                    "<peer><uuid>85c42b0d-c2b7-424a-ae72-5174c25da40b</uuid><hostname>testserver2</hostname><connected>1</connected><state>3</state></peer></peerStatus></cliOutput>";
    private SSHClient clientMock;
    private VdsStaticDAO vdsStaticDaoMock;

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
        vdsStaticDaoMock = mock(VdsStaticDAO.class);
        doReturn(vdsStaticDaoMock).when(getQuery()).getVdsStaticDao();
        doReturn(getVdsStatic().get(0)).when(vdsStaticDaoMock).getByHostName(NEW_SERVER);
        doReturn(getVdsStatic()).when(vdsStaticDaoMock).getAllWithIpAddress(NEW_SERVER);

        clientMock = mock(SSHClient.class);
        doReturn(clientMock).when(getQuery()).createSSHClient();
    }

    private List<VdsStatic> getVdsStatic() {
        VdsStatic vds = new VdsStatic();
        vds.setId(new Guid());
        vds.setHostName(NEW_SERVER);
        return Collections.singletonList(vds);
    }

    private void setupExpectedFingerPrint() throws Exception {
        doReturn(SERVER_NAME1).when(getQueryParameters()).getServerName();
        doReturn(PASSWORD).when(getQueryParameters()).getPassword();
        doReturn(FINGER_PRINT1).when(getQueryParameters()).getFingerprint();
        doReturn(clientMock).when(getQuery()).connect(SERVER_NAME1);
        doReturn(OUTPUT_XML).when(getQuery()).executeCommand(clientMock);
        doNothing().when(getQuery()).authenticate(clientMock, "root", PASSWORD);
        doNothing().when(getQuery()).validateFingerprint(clientMock, FINGER_PRINT1);

        EXPECTED_MAP.put(SERVER_NAME1, FINGER_PRINT1);
        EXPECTED_MAP.put(SERVER_NAME2, FINGER_PRINT1);
        doReturn(EXPECTED_MAP).when(getQuery()).extractServers(OUTPUT_XML);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testExecuteQueryCommand() {
        getQuery().executeQueryCommand();
        Map<String, String> serverFingerprintMap =
                (Map<String, String>) getQuery().getQueryReturnValue().getReturnValue();

        assertNotNull(serverFingerprintMap);
        assertEquals(EXPECTED_MAP, serverFingerprintMap);
    }
}
