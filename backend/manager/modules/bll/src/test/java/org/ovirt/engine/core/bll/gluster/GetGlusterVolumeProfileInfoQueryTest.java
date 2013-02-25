package org.ovirt.engine.core.bll.gluster;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.AbstractQueryTest;
import org.ovirt.engine.core.bll.utils.ClusterUtils;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.gluster.BlockStats;
import org.ovirt.engine.core.common.businessentities.gluster.BrickProfileDetails;
import org.ovirt.engine.core.common.businessentities.gluster.FopStats;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeProfileInfo;
import org.ovirt.engine.core.common.businessentities.gluster.StatsInfo;
import org.ovirt.engine.core.common.queries.gluster.GlusterVolumeQueriesParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;

@RunWith(MockitoJUnitRunner.class)
public class GetGlusterVolumeProfileInfoQueryTest extends
        AbstractQueryTest<GlusterVolumeQueriesParameters, GetGlusterVolumeProfileInfoQuery<GlusterVolumeQueriesParameters>> {

    private static final Guid CLUSTER_ID = new Guid("b399944a-81ab-4ec5-8266-e19ba7c3c9d1");
    private static final Guid VOLUME_ID = new Guid("b399944a-81ab-4ec5-8266-e19ba7c3c943");
    private GlusterVolumeProfileInfo expectedProfileInfo;
    private GlusterVolumeQueriesParameters params;

    @Mock
    private VdsDAO vdsDao;

    @Mock
    private ClusterUtils clusterUtils;

    @Mock
    private GlusterVolumeDao volumeDao;

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
        vds.setId(new Guid());
        vds.setVdsName("gfs1");
        vds.setVdsGroupId(CLUSTER_ID);
        vds.setStatus(status);
        return vds;
    }

    public void mockVdsDbFacadeAndDao() {
        doReturn(Collections.singletonList(getVds(VDSStatus.Up))).when(vdsDao).getAllForVdsGroupWithStatus(CLUSTER_ID,
                VDSStatus.Up);
        doReturn(volumeDao).when(getQuery()).getGlusterVolumeDao();
        doReturn(clusterUtils).when(getQuery()).getClusterUtils();
        doReturn(getVds(VDSStatus.Up)).when(clusterUtils).getUpServer(CLUSTER_ID);
        doReturn(vdsDao).when(clusterUtils).getVdsDao();
        doReturn("test-vol").when(getQuery()).getGlusterVolumeName(VOLUME_ID);
    }

    private void setupMock() {
        VDSReturnValue returnValue = new VDSReturnValue();
        returnValue.setSucceeded(true);
        returnValue.setReturnValue(expectedProfileInfo);
        doReturn(returnValue).when(getQuery()).runVdsCommand(eq(VDSCommandType.GetGlusterVolumeProfileInfo),
                any(VDSParametersBase.class));
    }

    private void setupExpectedGlusterVolumeOptionInfo() {
        params = new GlusterVolumeQueriesParameters(CLUSTER_ID, VOLUME_ID);
        expectedProfileInfo = new GlusterVolumeProfileInfo();
        expectedProfileInfo.setVolumeId(VOLUME_ID);
        expectedProfileInfo.setBrickProfileDetails(getBrickProfileDetails());
    }

    private List<BrickProfileDetails> getBrickProfileDetails() {
        BrickProfileDetails profileDetails = new BrickProfileDetails();
        profileDetails.setBrickId(new Guid());
        profileDetails.setStatsInfo(getStatsInfo());
        return Collections.singletonList(profileDetails);
    }

    private List<StatsInfo> getStatsInfo() {
        StatsInfo statInfo = new StatsInfo();
        statInfo.setDuration(2);
        statInfo.setTotalRead(0);
        statInfo.setTotalWrite(0);
        statInfo.setBlockStats(getBlockStats());
        statInfo.setFopStats(getFopStats());
        return Collections.singletonList(statInfo);
    }

    private List<FopStats> getFopStats() {
        FopStats fopStats = new FopStats();
        fopStats.setAvgLatency(78.12500);
        fopStats.setName("STATFS");
        fopStats.setHits(2);
        fopStats.setMinLatency(39.00000);
        fopStats.setMaxLatency(143.00000);
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
        getQuery().executeQueryCommand();
        GlusterVolumeProfileInfo glusterVolumeProfileInfo =
                (GlusterVolumeProfileInfo) getQuery().getQueryReturnValue().getReturnValue();

        assertNotNull(glusterVolumeProfileInfo);
        assertEquals(expectedProfileInfo, glusterVolumeProfileInfo);
    }
}
