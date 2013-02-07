package org.ovirt.engine.core.bll.gluster;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.bll.AbstractQueryTest;
import org.ovirt.engine.core.bll.utils.ClusterUtils;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.gluster.BrickDetails;
import org.ovirt.engine.core.common.businessentities.gluster.BrickProperties;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterClientInfo;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeAdvancedDetails;
import org.ovirt.engine.core.common.businessentities.gluster.MallInfo;
import org.ovirt.engine.core.common.businessentities.gluster.MemoryStatus;
import org.ovirt.engine.core.common.businessentities.gluster.Mempool;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.queries.gluster.GlusterVolumeAdvancedDetailsParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDAO;

public class GetGlusterVolumeAdvancedDetailsQueryTest extends
        AbstractQueryTest<GlusterVolumeAdvancedDetailsParameters, GetGlusterVolumeAdvancedDetailsQuery<GlusterVolumeAdvancedDetailsParameters>> {

    private static final Guid CLUSTER_ID = new Guid("b399944a-81ab-4ec5-8266-e19ba7c3c9d1");
    private static final String VOLUME_NAME = "test-volume1";
    private static final String BRICK_NAME = "test-server1:/tmp/b1";
    private static final String SERVER_NAME = "server1";
    private GlusterVolumeAdvancedDetails expectedVolumeAdvancedDetails;
    private ClusterUtils clusterUtils;
    private VDSBrokerFrontend vdsBrokerFrontend;
    private VdsDAO vdsDao;
    private GlusterVolumeAdvancedDetailsParameters parameters;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        setupExpectedVolume();
        setupMock();
    }

    private void setupExpectedVolume() {
        parameters = new GlusterVolumeAdvancedDetailsParameters(CLUSTER_ID, VOLUME_NAME, BRICK_NAME, false);
        expectedVolumeAdvancedDetails = new GlusterVolumeAdvancedDetails();
        expectedVolumeAdvancedDetails.setVolumeId(Guid.NewGuid());
        expectedVolumeAdvancedDetails.setBrickDetails(getBrickDetails());
    }

    private List<BrickDetails> getBrickDetails() {
        BrickDetails brickDetails = new BrickDetails();
        brickDetails.setBrickProperties(getBrickProperties());
        brickDetails.setClients(getClientInfo());
        brickDetails.setMemoryStatus(getMemoryStatus());
        return Collections.singletonList(brickDetails);
    }

    private BrickProperties getBrickProperties() {
        BrickProperties brickProperties = new BrickProperties();
        brickProperties.setBrickId(Guid.NewGuid());
        brickProperties.setPort(24009);
        brickProperties.setStatus(GlusterStatus.UP);
        brickProperties.setPid(1459);
        return brickProperties;
    }

    private List<GlusterClientInfo> getClientInfo() {
        GlusterClientInfo clientInfo = new GlusterClientInfo();
        clientInfo.setBytesRead(836);
        clientInfo.setBytesWritten(468);
        clientInfo.setHostname(SERVER_NAME + ":1006");
        return Collections.singletonList(clientInfo);
    }

    private MemoryStatus getMemoryStatus() {
        MemoryStatus memoryStatus = new MemoryStatus();
        memoryStatus.setMallInfo(getMallInfo());
        memoryStatus.setMemPools(getMemPools());
        return memoryStatus;
    }

    private List<Mempool> getMemPools() {
        Mempool memPool = new Mempool();
        memPool.setAllocCount(0);
        memPool.setColdCount(1024);
        memPool.setHotCount(0);
        memPool.setMaxAlloc(0);
        memPool.setMaxStdAlloc(0);
        memPool.setName("v1-server:fd_t");
        memPool.setPadddedSize(100);
        memPool.setPoolMisses(0);
        return Collections.singletonList(memPool);
    }

    private MallInfo getMallInfo() {
        MallInfo mallInfo = new MallInfo();
        mallInfo.setArena(606208);
        mallInfo.setFordblks(110336);
        mallInfo.setFsmblks(0);
        mallInfo.setHblkhd(15179776);
        mallInfo.setOrdblks(1);
        mallInfo.setSmblks(0);
        mallInfo.setUordblks(495872);
        mallInfo.setUsmblks(0);
        return mallInfo;
    }

    private VDS getVds(VDSStatus status) {
        VDS vds = new VDS();
        vds.setId(new Guid());
        vds.setVdsName("gfs1");
        vds.setVdsGroupId(CLUSTER_ID);
        vds.setStatus(status);
        return vds;
    }

    private void setupMock() {
        clusterUtils = mock(ClusterUtils.class);
        vdsDao = mock(VdsDAO.class);
        doReturn(clusterUtils).when(getQuery()).getClusterUtils();
        doReturn(getVds(VDSStatus.Up)).when(clusterUtils).getUpServer(CLUSTER_ID);
        doReturn(vdsDao).when(clusterUtils).getVdsDao();
        when(vdsDao.getAllForVdsGroupWithStatus(CLUSTER_ID, VDSStatus.Up)).thenReturn(Collections.singletonList(getVds(VDSStatus.Up)));

        vdsBrokerFrontend = mock(VDSBrokerFrontend.class);
        doReturn(vdsBrokerFrontend).when(getQuery()).getBackendResourceManager();

        // Mock the query's parameters
        doReturn(CLUSTER_ID).when(getQueryParameters()).getClusterId();
        doReturn(VOLUME_NAME).when(getQueryParameters()).getVolumeName();
        doReturn(BRICK_NAME).when(getQueryParameters()).getBrickName();
        doReturn(true).when(getQueryParameters()).isDetailRequired();

        VDSReturnValue returnValue = new VDSReturnValue();
        returnValue.setSucceeded(true);
        returnValue.setReturnValue(expectedVolumeAdvancedDetails);
        when(vdsBrokerFrontend.RunVdsCommand(eq(VDSCommandType.GetGlusterVolumeAdvancedDetails),
                any(VDSParametersBase.class))).thenReturn(returnValue);

    }

    @Test
    public void testExecuteQueryCommnad() {
        getQuery().executeQueryCommand();
        GlusterVolumeAdvancedDetails volumeAdvancedDetails =
                (GlusterVolumeAdvancedDetails) getQuery().getQueryReturnValue().getReturnValue();

        assertNotNull(volumeAdvancedDetails);
        assertEquals(expectedVolumeAdvancedDetails, volumeAdvancedDetails);
    }
}
