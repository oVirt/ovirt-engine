package org.ovirt.engine.core.bll.gluster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.AbstractQueryTest;
import org.ovirt.engine.core.bll.utils.GlusterGeoRepUtil;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepNonEligibilityReason;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSizeInfo;
import org.ovirt.engine.core.common.queries.gluster.GlusterVolumeGeoRepEligibilityParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.gluster.GlusterGeoRepDao;

@MockitoSettings(strictness = Strictness.LENIENT)
public class GetNonEligibilityReasonsOfVolumeForGeoRepSessionQueryTest extends AbstractQueryTest<GlusterVolumeGeoRepEligibilityParameters, GetNonEligibilityReasonsOfVolumeForGeoRepSessionQuery<GlusterVolumeGeoRepEligibilityParameters>> {

    @Mock
    private GlusterGeoRepDao geoRepDao;

    @Mock
    private ClusterDao clusterDao;

    private GeoRepCreateEligibilityBaseTest baseTest = new GeoRepCreateEligibilityBaseTest();

    @Spy
    @InjectMocks
    private GlusterGeoRepUtil geoRepUtil;

    @BeforeEach
    public void setupMock() {
        doReturn(geoRepUtil).when(getQuery()).getGeoRepUtilInstance();
        baseTest.setupMock(geoRepDao, clusterDao);
    }

    protected List<GlusterGeoRepNonEligibilityReason> getNonEligibilityReasonsForSlaveVolume2() {
        List<GlusterGeoRepNonEligibilityReason> nonEligibilityreasons = new ArrayList<>();

        nonEligibilityreasons.add(GlusterGeoRepNonEligibilityReason.SLAVE_VOLUME_SHOULD_BE_UP);
        nonEligibilityreasons.add(GlusterGeoRepNonEligibilityReason.SLAVE_VOLUME_SIZE_SHOULD_BE_GREATER_THAN_MASTER_VOLUME_SIZE);
        nonEligibilityreasons.add(GlusterGeoRepNonEligibilityReason.SLAVE_VOLUME_SHOULD_NOT_BE_SLAVE_OF_ANOTHER_GEO_REP_SESSION);
        nonEligibilityreasons.add(GlusterGeoRepNonEligibilityReason.SLAVE_VOLUME_TO_BE_EMPTY);

        return nonEligibilityreasons;
    }

    protected List<GlusterGeoRepNonEligibilityReason> getNonEligibilityReasonsForSlaveVolume3() {
        List<GlusterGeoRepNonEligibilityReason> nonEligibilityreasons = new ArrayList<>();

        nonEligibilityreasons.add(GlusterGeoRepNonEligibilityReason.SLAVE_AND_MASTER_VOLUMES_SHOULD_NOT_BE_IN_SAME_CLUSTER);

        return nonEligibilityreasons;
    }

    protected List<GlusterGeoRepNonEligibilityReason> getNonEligibilityReasonsForSlaveVolume4() {
        List<GlusterGeoRepNonEligibilityReason> nonEligibilityreasons = new ArrayList<>();

        nonEligibilityreasons.add(GlusterGeoRepNonEligibilityReason.SLAVE_VOLUME_SIZE_TO_BE_AVAILABLE);
        nonEligibilityreasons.add(GlusterGeoRepNonEligibilityReason.MASTER_VOLUME_SIZE_TO_BE_AVAILABLE);
        nonEligibilityreasons.add(GlusterGeoRepNonEligibilityReason.SLAVE_VOLUME_SIZE_SHOULD_BE_GREATER_THAN_MASTER_VOLUME_SIZE);
        nonEligibilityreasons.add(GlusterGeoRepNonEligibilityReason.NO_UP_SLAVE_SERVER);
        nonEligibilityreasons.add(GlusterGeoRepNonEligibilityReason.SLAVE_VOLUME_TO_BE_EMPTY);

        return nonEligibilityreasons;
    }

    @Test
    public void testExecuteQueryCommnadOnVolume1() {
        GlusterVolumeEntity slaveVolume = baseTest.getGlusterVolume(baseTest.getSLAVE_VOLUME_1_ID(), baseTest.getSLAVE_CLUSTER_ID(), GlusterStatus.UP, new GlusterVolumeSizeInfo(10000L, 4000L, 6000L));
        Guid slaveUpServerId = Guid.newGuid();
        doReturn(slaveUpServerId).when(geoRepUtil).getUpServerId(any());
        doReturn(true).when(geoRepUtil).checkEmptyGlusterVolume(slaveUpServerId, slaveVolume.getName());
        List<GlusterGeoRepNonEligibilityReason> actualNonEligibilityReasons = getQuery().getNonEligibilityReasons(baseTest.getGlusterVolume(baseTest.getMASTER_VOLUME_ID(), baseTest.getMASTER_CLUSTER_ID(), GlusterStatus.UP, new GlusterVolumeSizeInfo(10000L, 4000L, 6000L)), slaveVolume);
        assertTrue(actualNonEligibilityReasons.isEmpty());
    }

    @Test
    public void testExecuteQueryCommnadOnVolume2() {
        GlusterVolumeEntity slaveVolume = baseTest.getGlusterVolume(baseTest.getSLAVE_VOLUME_2_ID(), baseTest.getSLAVE_CLUSTER_ID(), GlusterStatus.DOWN, new GlusterVolumeSizeInfo(4000L, 0L, 0L));
        Guid slaveUpServerID = Guid.newGuid();
        doReturn(slaveUpServerID).when(geoRepUtil).getUpServerId(any());
        doReturn(false).when(geoRepUtil).checkEmptyGlusterVolume(slaveUpServerID, slaveVolume.getName());
        List<GlusterGeoRepNonEligibilityReason> actual = getQuery().getNonEligibilityReasons(baseTest.getGlusterVolume(baseTest.getMASTER_VOLUME_ID(), baseTest.getMASTER_CLUSTER_ID(), GlusterStatus.UP, new GlusterVolumeSizeInfo(10000L, 4000L, 6000L)), slaveVolume);
        assertEquals(actual.size(), getNonEligibilityReasonsForSlaveVolume2().size());
        assertTrue(getNonEligibilityReasonsForSlaveVolume2().containsAll(actual));
    }

    @Test
    public void testExecuteQueryCommnadOnVolume3() {
        GlusterVolumeEntity slaveVolume = baseTest.getGlusterVolume(baseTest.getSLAVE_VOLUME_3_ID(), baseTest.getMASTER_CLUSTER_ID(), GlusterStatus.UP, new GlusterVolumeSizeInfo(10000L, 4000L, 6000L));
        Guid slaveUpServerId = Guid.newGuid();
        doReturn(slaveUpServerId).when(geoRepUtil).getUpServerId(any());
        doReturn(true).when(geoRepUtil).checkEmptyGlusterVolume(slaveUpServerId, slaveVolume.getName());
        List<GlusterGeoRepNonEligibilityReason> actual = getQuery().getNonEligibilityReasons(baseTest.getGlusterVolume(baseTest.getMASTER_VOLUME_ID(), baseTest.getMASTER_CLUSTER_ID(), GlusterStatus.UP, new GlusterVolumeSizeInfo(10000L, 4000L, 6000L)), slaveVolume);
        assertEquals(actual.size(), getNonEligibilityReasonsForSlaveVolume3().size());
        assertTrue(getNonEligibilityReasonsForSlaveVolume3().containsAll(actual));
    }

    @Test
    public void testExecuteQueryCommnadOnVolume4() {
        doReturn(null).when(geoRepUtil).getUpServerId(any());
        List<GlusterGeoRepNonEligibilityReason> actual = getQuery().getNonEligibilityReasons(baseTest.getGlusterVolume(baseTest.getMASTER_VOLUME_ID(), baseTest.getMASTER_CLUSTER_ID(), GlusterStatus.UP, null), baseTest.getGlusterVolume(baseTest.getSLAVE_VOLUME_4_ID(), baseTest.getSLAVE_CLUSTER_ID(), GlusterStatus.UP, null));
        assertEquals(actual.size(), getNonEligibilityReasonsForSlaveVolume4().size());
        assertTrue(getNonEligibilityReasonsForSlaveVolume4().containsAll(actual));
    }
}
