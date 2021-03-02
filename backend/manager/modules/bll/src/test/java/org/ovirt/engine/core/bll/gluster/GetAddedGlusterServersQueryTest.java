package org.ovirt.engine.core.bll.gluster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.AbstractQueryTest;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.utils.GlusterUtil;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerInfo;
import org.ovirt.engine.core.common.businessentities.gluster.PeerStatus;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.queries.gluster.AddedGlusterServersParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.gluster.GlusterDBUtils;

public class GetAddedGlusterServersQueryTest extends AbstractQueryTest<AddedGlusterServersParameters, GetAddedGlusterServersQuery<AddedGlusterServersParameters>> {
    private List<VDS> serversList;
    private List<GlusterServerInfo> expectedServers;
    private AddedGlusterServersParameters params;

    private static final String CLUSTER_NAME = "default";
    private static final String TEST_SERVER1 = "test_server1";
    private static final String TEST_SERVER2 = "test_server2";
    private static final String TEST_SERVER3 = "test_server3";
    private static final Guid CLUSTER_ID = new Guid("b399944a-81ab-4ec5-8266-e19ba7c3c9d1");
    private static final Guid server_id1 = new Guid("85c42b0d-c2b7-424a-ae72-5174c25da40b");
    private static final Guid server_id2 = new Guid("6a697a38-cc82-4399-a6fb-0ec79c0ff1d5");
    private static final Guid server_id3 = new Guid("7a797a38-cb32-4399-b6fb-21c79c03a1d6");
    private static final String serverSshPublicKey = "test-pk";

    @Mock
    private VDSBrokerFrontend vdsBrokerFrontend;

    @Mock
    private BackendInternal backendInternal;

    @Mock
    private GlusterUtil glusterUtils;

    @Mock
    private GlusterDBUtils dbUtils;

    private VDS getVds(VDSStatus status) {
        VDS vds = new VDS();
        vds.setId(Guid.newGuid());
        vds.setVdsName("gfs1");
        vds.setClusterId(CLUSTER_ID);
        vds.setStatus(status);
        return vds;
    }

    private void setupParams() {
        params = new AddedGlusterServersParameters(CLUSTER_ID, true);
    }

    private void setupServersList() {
        serversList = new ArrayList<>();
        VDS server = new VDS();
        server.setClusterId(CLUSTER_ID);
        server.setClusterName(CLUSTER_NAME);
        server.setId(server_id1);
        server.setVdsName(TEST_SERVER1);
        server.setHostName(TEST_SERVER1);
        serversList.add(server);

        server = new VDS();
        server.setClusterId(CLUSTER_ID);
        server.setClusterName(CLUSTER_NAME);
        server.setId(server_id2);
        server.setVdsName(TEST_SERVER2);
        server.setHostName(TEST_SERVER2);
        serversList.add(server);
    }

    private void setupExpectedGlusterServersInfo() {
        expectedServers = new ArrayList<>();
        GlusterServerInfo server = new GlusterServerInfo();
        server.setUuid(server_id3);
        server.setHostnameOrIp(TEST_SERVER3);
        server.setStatus(PeerStatus.CONNECTED);
        expectedServers.add(server);
    }

    private void setupMock() {
        doReturn(getVds(VDSStatus.Up)).when(glusterUtils).getUpServer(CLUSTER_ID);

        VDSReturnValue returnValue = getVDSReturnValue();
        when(vdsBrokerFrontend.runVdsCommand(eq(VDSCommandType.GlusterServersList), any())).thenReturn(returnValue);
        QueryReturnValue vdcReturnValue = getVdcReturnValue();
        when(backendInternal.runInternalQuery(
                eq(QueryType.GetServerSSHPublicKey), any(), any())).thenReturn(vdcReturnValue);
        doReturn(params.getClusterId()).when(getQueryParameters()).getClusterId();
        doReturn(true).when(getQueryParameters()).isServerPublicKeyRequired();
    }

    private QueryReturnValue getVdcReturnValue() {
        QueryReturnValue retValue = new QueryReturnValue();
        retValue.setSucceeded(true);
        retValue.setReturnValue(serverSshPublicKey);
        return retValue;
    }

    private VDSReturnValue getVDSReturnValue() {
        VDSReturnValue returnValue = new VDSReturnValue();
        returnValue.setSucceeded(true);
        returnValue.setReturnValue(expectedServers);
        return returnValue;
    }

    private Map<String, String> getAddedServers() {
        Map<String, String> servers = new HashMap<>();
        servers.put(TEST_SERVER3, serverSshPublicKey);
        return servers;
    }

    @BeforeEach
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
    public void testExecuteQueryCommand() {
        getQuery().executeQueryCommand();
        Map<String, String> servers = getQuery().getQueryReturnValue().getReturnValue();

        assertNotNull(servers);
        assertEquals(getAddedServers(), servers);
    }
}
