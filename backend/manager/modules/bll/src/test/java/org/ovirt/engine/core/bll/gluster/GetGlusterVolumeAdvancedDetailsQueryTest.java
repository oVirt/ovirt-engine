package org.ovirt.engine.core.bll.gluster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.AbstractQueryTest;
import org.ovirt.engine.core.bll.utils.GlusterUtil;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.gluster.BrickDetails;
import org.ovirt.engine.core.common.businessentities.gluster.BrickProperties;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterClientInfo;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeAdvancedDetails;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.MallInfo;
import org.ovirt.engine.core.common.businessentities.gluster.MemoryStatus;
import org.ovirt.engine.core.common.businessentities.gluster.Mempool;
import org.ovirt.engine.core.common.queries.gluster.GlusterVolumeAdvancedDetailsParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.gluster.GlusterBrickDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;

@MockitoSettings(strictness = Strictness.LENIENT)
public class GetGlusterVolumeAdvancedDetailsQueryTest extends
        AbstractQueryTest<GlusterVolumeAdvancedDetailsParameters, GetGlusterVolumeAdvancedDetailsQuery<GlusterVolumeAdvancedDetailsParameters>> {

    private static final Guid CLUSTER_ID = new Guid("b399944a-81ab-4ec5-8266-e19ba7c3c9d1");
    private static final Guid VOLUME_ID = Guid.newGuid();
    private static final Guid BRICK_ID = Guid.newGuid();
    private static final Guid SERVER_ID = Guid.newGuid();
    private static final String SERVER_NAME = "server1";
    private GlusterVolumeAdvancedDetails expectedVolumeAdvancedDetails;

    @Mock
    private GlusterUtil glusterUtils;

    @Mock
    private VdsDao vdsDao;

    @Mock
    private GlusterVolumeDao volumeDao;

    @Mock
    private GlusterBrickDao brickDao;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        setupExpectedVolume();
        setupMock();
    }

    private void setupExpectedVolume() {
        expectedVolumeAdvancedDetails = new GlusterVolumeAdvancedDetails();
        expectedVolumeAdvancedDetails.setVolumeId(VOLUME_ID);
        expectedVolumeAdvancedDetails.setBrickDetails(getBrickDetails());
    }

    private GlusterVolumeEntity getVolume() {
        GlusterVolumeEntity volume = new GlusterVolumeEntity();
        volume.setId(VOLUME_ID);
        return volume;
    }

    private GlusterBrickEntity getBrick() {
        BrickProperties brickProperties = new BrickProperties();
        brickProperties.setConfirmedFreeSize(1.0);
        BrickDetails details = new BrickDetails();
        details.setBrickProperties(brickProperties);

        GlusterBrickEntity brick = new GlusterBrickEntity();
        brick.setId(BRICK_ID);
        brick.setServerId(SERVER_ID);
        brick.setBrickDetails(details);
        return brick;
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
        brickProperties.setBrickId(BRICK_ID);
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
        vds.setId(Guid.newGuid());
        vds.setVdsName("gfs1");
        vds.setClusterId(CLUSTER_ID);
        vds.setStatus(status);
        return vds;
    }

    private void setupMock() {
        when(volumeDao.getById(VOLUME_ID)).thenReturn(getVolume());
        when(brickDao.getById(BRICK_ID)).thenReturn(getBrick());

        // Mock the query's parameters. Note that the brick id is
        // mocked inside the test methods to test different scenarios.
        doReturn(CLUSTER_ID).when(getQueryParameters()).getClusterId();
        doReturn(true).when(getQueryParameters()).isDetailRequired();

        VDSReturnValue returnValue = new VDSReturnValue();
        returnValue.setSucceeded(true);
        returnValue.setReturnValue(expectedVolumeAdvancedDetails);
        doReturn(returnValue).when(getQuery()).runVdsCommand(eq(VDSCommandType.GetGlusterVolumeAdvancedDetails), any());
    }

    @Test
    public void testQueryForBrickDetails() {
        doReturn(VOLUME_ID).when(getQueryParameters()).getVolumeId();
        doReturn(BRICK_ID).when(getQueryParameters()).getBrickId();
        when(vdsDao.get(SERVER_ID)).thenReturn(getVds(VDSStatus.Up));

        getQuery().executeQueryCommand();
        GlusterVolumeAdvancedDetails volumeAdvancedDetails = getQuery().getQueryReturnValue().getReturnValue();

        assertNotNull(volumeAdvancedDetails);
        assertEquals(expectedVolumeAdvancedDetails, volumeAdvancedDetails);

        // Server is fetched directly from the brick's server,
        // and clusterUtils is not used to fetch a random UP server
        verify(vdsDao, times(1)).get(SERVER_ID);
        verifyZeroInteractions(glusterUtils);
    }

    @Test
    public void testQueryForInvalidVolumeId() {
        doReturn(Guid.Empty).when(getQueryParameters()).getVolumeId();

        assertThrows(RuntimeException.class, () -> getQuery().executeQueryCommand());
    }

    @Test
    public void testQueryForNullBrickId() {
        doReturn(VOLUME_ID).when(getQueryParameters()).getVolumeId();
        doReturn(null).when(getQueryParameters()).getBrickId();
        doReturn(getVds(VDSStatus.Up)).when(glusterUtils).getRandomUpServer(CLUSTER_ID);

        getQuery().executeQueryCommand();
        GlusterVolumeAdvancedDetails volumeAdvancedDetails = getQuery().getQueryReturnValue().getReturnValue();

        assertNotNull(volumeAdvancedDetails);
        assertEquals(expectedVolumeAdvancedDetails, volumeAdvancedDetails);

        // Brick's server is not fetched, rather clusterUtil is used to fetch a random UP server
        verifyZeroInteractions(vdsDao);
        verify(glusterUtils, times(1)).getRandomUpServer(CLUSTER_ID);
    }
}
