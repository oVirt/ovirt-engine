package org.ovirt.engine.core.bll.gluster;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.bll.AbstractQueryTest;
import org.ovirt.engine.core.bll.utils.ClusterUtils;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerInfo;
import org.ovirt.engine.core.common.businessentities.gluster.PeerStatus;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.queries.gluster.AddedGlusterServersParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.utils.ssh.EngineSSHDialog;

public class GetAddedGlusterServersQueryTest extends AbstractQueryTest<AddedGlusterServersParameters, GetAddedGlusterServersQuery<AddedGlusterServersParameters>> {

    List<VDS> serversList;
    List<GlusterServerInfo> expectedServers;
    AddedGlusterServersParameters params;
    private Guid CLUSTER_ID = new Guid("b399944a-81ab-4ec5-8266-e19ba7c3c9d1");
    private Guid server_id1 = new Guid("85c42b0d-c2b7-424a-ae72-5174c25da40b");
    private Guid server_id2 = new Guid("6a697a38-cc82-4399-a6fb-0ec79c0ff1d5");
    private Guid server_id3 = new Guid("7a797a38-cb32-4399-b6fb-21c79c03a1d6");
    private String serverKeyFingerprint = "b5:ad:16:19:06:9f:b3:41:69:eb:1c:42:1d:12:b5:31";
    VDSBrokerFrontend vdsBrokerFrontend;
    VdsDAO vdsDaoMock;
    EngineSSHDialog mockEngineSSHDialog;

    ClusterUtils clusterUtils;

    private VDS getVds(VDSStatus status) {
        VDS vds = new VDS();
        vds.setId(new Guid());
        vds.setVdsName("gfs1");
        vds.setVdsGroupId(CLUSTER_ID);
        vds.setStatus(status);
        return vds;
    }

    private void setupParams() {
        params = new AddedGlusterServersParameters(CLUSTER_ID, true);
    }

    private void setupServersList() {
        serversList = new ArrayList<VDS>();
        VDS server = new VDS();
        server.setVdsGroupId(CLUSTER_ID);
        server.setVdsGroupName("default");
        server.setId(server_id1);
        server.setVdsName("test_server1");
        server.setHostName("test_server1");
        serversList.add(server);

        server = new VDS();
        server.setVdsGroupId(CLUSTER_ID);
        server.setVdsGroupName("default");
        server.setId(server_id2);
        server.setVdsName("test_server2");
        server.setHostName("test_server2");
        serversList.add(server);
    }

    private void setupExpectedGlusterServersInfo() {
        expectedServers = new ArrayList<GlusterServerInfo>();
        GlusterServerInfo server = new GlusterServerInfo();
        server.setUuid(server_id1);
        server.setHostnameOrIp("test_server1");
        server.setStatus(PeerStatus.CONNECTED);
        expectedServers.add(server);
        server = new GlusterServerInfo();
        server.setUuid(server_id3);
        server.setHostnameOrIp("test_server3");
        server.setStatus(PeerStatus.CONNECTED);
        expectedServers.add(server);
    }

    private void setupMock() throws Exception {
        vdsBrokerFrontend = mock(VDSBrokerFrontend.class);
        clusterUtils = mock(ClusterUtils.class);
        vdsDaoMock = mock(VdsDAO.class);

        doReturn(vdsBrokerFrontend).when(getQuery()).getBackendInstance();
        doReturn(clusterUtils).when(getQuery()).getClusterUtils();
        doReturn(getVds(VDSStatus.Up)).when(clusterUtils).getUpServer(CLUSTER_ID);

        VDSReturnValue returnValue = getVDSReturnValue();
        when(vdsBrokerFrontend.RunVdsCommand(eq(VDSCommandType.GlusterServersList),
                any(VDSParametersBase.class))).thenReturn(returnValue);
        doReturn(params.getClusterId()).when(getQueryParameters()).getClusterId();
        doReturn(true).when(getQueryParameters()).isServerKeyFingerprintRequired();

        doReturn(vdsDaoMock).when(clusterUtils).getVdsDao();
        doReturn(serversList).when(vdsDaoMock).getAllForVdsGroup(CLUSTER_ID);

        mockEngineSSHDialog = mock(EngineSSHDialog.class);
        doNothing().when(mockEngineSSHDialog).connect();
        doNothing().when(mockEngineSSHDialog).authenticate();
        doReturn(mockEngineSSHDialog).when(getQuery()).getEngineSSHDialog();
    }

    private VDSReturnValue getVDSReturnValue() {
        VDSReturnValue returnValue = new VDSReturnValue();
        returnValue.setSucceeded(true);
        returnValue.setReturnValue(expectedServers);
        return returnValue;
    }

    private Map<String, String> getAddedServers() {
        Map<String, String> servers = new HashMap<String, String>();
        servers.put("test_server3", serverKeyFingerprint);
        return servers;
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        setupParams();
        setupServersList();
        setupExpectedGlusterServersInfo();
        setupMock();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testExecuteQueryCommand() throws IOException {
        doReturn(serverKeyFingerprint).when(mockEngineSSHDialog).getHostFingerprint();
        getQuery().executeQueryCommand();
        Map<String, String> servers =
                (Map<String, String>) getQuery().getQueryReturnValue().getReturnValue();

        assertNotNull(servers);
        assertEquals(getAddedServers(), servers);
    }
}
