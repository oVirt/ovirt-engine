package org.ovirt.engine.core.bll.gluster;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.AbstractQueryTest;
import org.ovirt.engine.core.bll.utils.GlusterUtil;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.gluster.BlockStats;
import org.ovirt.engine.core.common.businessentities.gluster.BrickProfileDetails;
import org.ovirt.engine.core.common.businessentities.gluster.FopStats;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeProfileInfo;
import org.ovirt.engine.core.common.businessentities.gluster.StatsInfo;
import org.ovirt.engine.core.common.queries.gluster.GlusterVolumeProfileParameters;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.gluster.GlusterBrickDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;

@RunWith(MockitoJUnitRunner.class)
public class GetGlusterVolumeProfileInfoQueryTest extends
        AbstractQueryTest<GlusterVolumeProfileParameters, GetGlusterVolumeProfileInfoQuery<GlusterVolumeProfileParameters>> {

    private static final Guid CLUSTER_ID = new Guid("b399944a-81ab-4ec5-8266-e19ba7c3c9d1");
    private static final Guid VOLUME_ID = new Guid("b399944a-81ab-4ec5-8266-e19ba7c3c943");
    private GlusterVolumeProfileInfo expectedProfileInfo;
    private GlusterVolumeProfileParameters params;

    @Mock
    private VdsDao vdsDao;

    @Mock
    private GlusterUtil glusterUtils;

    @Mock
    private GlusterVolumeDao volumeDao;

    @Mock
    private GlusterBrickDao brickDao;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        mockVdsDbFacadeAndDao();
        setupExpectedGlusterVolumeOptionInfo();
        setupMock();
    }

    private VDS getVds(VDSStatus status) {
        VDS vds = new VDS();
        vds.setId(Guid.newGuid());
        vds.setVdsName("gfs1");
        vds.setClusterId(CLUSTER_ID);
        vds.setStatus(status);
        return vds;
    }

    private GlusterBrickEntity getBrick() {
        GlusterBrickEntity brick = new GlusterBrickEntity();
        brick.setBrickDirectory("dir");
        brick.setServerName("host");
        return brick;
    }

    private void mockVdsDbFacadeAndDao() {
        doReturn(Collections.singletonList(getVds(VDSStatus.Up))).when(vdsDao).getAllForClusterWithStatus(CLUSTER_ID,
                VDSStatus.Up);
        doReturn(volumeDao).when(getQuery()).getGlusterVolumeDao();
        doReturn(brickDao).when(getQuery()).getGlusterBrickDao();
        doReturn(glusterUtils).when(getQuery()).getGlusterUtils();
        doReturn(getVds(VDSStatus.Up)).when(glusterUtils).getUpServer(CLUSTER_ID);
        doReturn("test-vol").when(getQuery()).getGlusterVolumeName(VOLUME_ID);
        doReturn(getBrick()).when(brickDao).getById(any(Guid.class));
    }

    private void setupMock() {
        VDSReturnValue returnValue = new VDSReturnValue();
        returnValue.setSucceeded(true);
        returnValue.setReturnValue(expectedProfileInfo);
        doReturn(returnValue).when(getQuery()).runVdsCommand(eq(VDSCommandType.GetGlusterVolumeProfileInfo),
                any(VDSParametersBase.class));
    }

    private void setupExpectedGlusterVolumeOptionInfo() {
        params = new GlusterVolumeProfileParameters(CLUSTER_ID, VOLUME_ID);
        expectedProfileInfo = new GlusterVolumeProfileInfo();
        expectedProfileInfo.setVolumeId(VOLUME_ID);
        expectedProfileInfo.setBrickProfileDetails(getBrickProfileDetails());
    }

    private List<BrickProfileDetails> getBrickProfileDetails() {
        BrickProfileDetails profileDetails = new BrickProfileDetails();
        profileDetails.setBrickId(Guid.newGuid());
        profileDetails.setProfileStats(getStatsInfo());
        return Collections.singletonList(profileDetails);
    }

    private List<StatsInfo> getStatsInfo() {
        StatsInfo statInfo = new StatsInfo();
        statInfo.setDuration(2);
        statInfo.setDurationFormatted(new Pair<>(2, TimeUnit.SECONDS.toString()));
        statInfo.setTotalRead(0);
        statInfo.setTotalWrite(0);
        statInfo.setBlockStats(getBlockStats());
        statInfo.setFopStats(getFopStats());
        return Collections.singletonList(statInfo);
    }

    private List<FopStats> getFopStats() {
        FopStats fopStats = new FopStats();
        fopStats.setAvgLatency(78.12500);
        fopStats.setAvgLatencyFormatted(new Pair<>(78.12500, TimeUnit.MICROSECONDS.toString()));
        fopStats.setName("STATFS");
        fopStats.setHits(2);
        fopStats.setMinLatency(39.00000);
        fopStats.setMinLatencyFormatted(new Pair<>(39.00000, TimeUnit.MICROSECONDS.toString()));
        fopStats.setMaxLatency(143.00000);
        fopStats.setMaxLatencyFormatted(new Pair<>(143.00000, TimeUnit.MICROSECONDS.toString()));
        return Collections.singletonList(fopStats);
    }

    private List<BlockStats> getBlockStats() {
        BlockStats blockStats = new BlockStats();
        blockStats.setSize(128);
        blockStats.setBlockRead(558);
        blockStats.setBlockWrite(12345);
        return Collections.singletonList(blockStats);
    }

    @Test
    public void testExecuteQueryCommand() {
        doReturn(params.getClusterId()).when(getQueryParameters()).getClusterId();
        doReturn(params.getVolumeId()).when(getQueryParameters()).getVolumeId();
        doReturn(params.isNfs()).when(getQueryParameters()).isNfs();
        getQuery().executeQueryCommand();
        GlusterVolumeProfileInfo glusterVolumeProfileInfo =
                getQuery().getQueryReturnValue().getReturnValue();

        assertNotNull(glusterVolumeProfileInfo);
        assertEquals(expectedProfileInfo, glusterVolumeProfileInfo);
    }
}
