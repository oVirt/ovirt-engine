package org.ovirt.engine.core.bll.gluster;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.AbstractQueryTest;
import org.ovirt.engine.core.bll.utils.GlusterGeoRepUtil;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSizeInfo;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.gluster.GlusterGeoRepDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;

@RunWith(MockitoJUnitRunner.class)
public class GetGlusterGeoReplicationEligibleVolumesQueryTest extends AbstractQueryTest<IdQueryParameters, GetGlusterGeoReplicationEligibleVolumesQuery<IdQueryParameters>> {

    @Mock
    private GlusterGeoRepDao geoRepDao;

    @Mock
    private ClusterDao clusterDao;

    @Mock
    private GlusterVolumeDao volumeDao;

    @Spy
    private GlusterGeoRepUtil geoRepUtil;

    private GeoRepCreateEligibilityBaseTest baseTest = new GeoRepCreateEligibilityBaseTest();

    @Before
    public void setupMock() {
        doReturn(geoRepUtil).when(getQuery()).getGeoRepUtilInstance();
        doReturn(Guid.newGuid()).when(geoRepUtil).getUpServerId(any(Guid.class));
        doReturn(true).when(geoRepUtil).checkEmptyGlusterVolume(any(Guid.class), anyString());
        doReturn(getExpectedVolumes()).when(getQuery()).getAllGlusterVolumesWithMasterCompatibleVersion(baseTest.getMASTER_VOLUME_ID());
        doReturn(volumeDao).when(getQuery()).getGlusterVolumeDao();
        doReturn(clusterDao).when(getQuery()).getClusterDao();
        baseTest.setupMock(geoRepUtil, geoRepDao, clusterDao);
        doReturn(getClustersByServiceAndCompatibilityVersion()).when(clusterDao).getClustersByServiceAndCompatibilityVersion(true, false, baseTest.getCLUSTER_COMPATIBILITY_VERSION().getValue());
        doReturn(getVolumesByClusterId()).when(volumeDao).getByClusterId(baseTest.getSLAVE_CLUSTER_ID());
        doReturn(baseTest.getMasterVolume()).when(volumeDao).getById(baseTest.getMASTER_VOLUME_ID());
    }

    private List<GlusterVolumeEntity> getVolumesByClusterId() {
        List<GlusterVolumeEntity> volumeInCluster = new ArrayList<>();

        volumeInCluster.add(baseTest.getGlusterVolume(baseTest.getSLAVE_VOLUME_1_ID(), baseTest.getSLAVE_CLUSTER_ID(), GlusterStatus.UP, new GlusterVolumeSizeInfo(10000L, 10000L, 0L)));
        volumeInCluster.add(baseTest.getGlusterVolume(baseTest.getSLAVE_VOLUME_2_ID(), baseTest.getSLAVE_CLUSTER_ID(), GlusterStatus.DOWN, new GlusterVolumeSizeInfo(4000L, 0L, 0L)));
        volumeInCluster.add(baseTest.getGlusterVolume(baseTest.getSLAVE_VOLUME_3_ID(), baseTest.getMASTER_CLUSTER_ID(), GlusterStatus.UP, new GlusterVolumeSizeInfo(10000L, 4000L, 6000L)));
        volumeInCluster.add(baseTest.getGlusterVolume(baseTest.getSLAVE_VOLUME_4_ID(), baseTest.getSLAVE_CLUSTER_ID(), GlusterStatus.UP, null));

        return volumeInCluster;
    }

    private List<Cluster> getClustersByServiceAndCompatibilityVersion() {
        List<Cluster> possiblyEligibleClusters = new ArrayList<>();

        Cluster possiblyEligibleCluster = new Cluster();
        possiblyEligibleCluster.setId(baseTest.getSLAVE_CLUSTER_ID());

        possiblyEligibleClusters.add(possiblyEligibleCluster);
        return possiblyEligibleClusters;
    }

    private List<GlusterVolumeEntity> getExpectedVolumes() {
        return Collections.singletonList(baseTest.getGlusterVolume(baseTest.getSLAVE_VOLUME_1_ID(), baseTest.getSLAVE_CLUSTER_ID(), GlusterStatus.UP, new GlusterVolumeSizeInfo(10000L, 10000L, 0L)));
    }

    private boolean checkEquals(List<GlusterVolumeEntity> actual, List<GlusterVolumeEntity> expected) {
        boolean equals = false;
        for(GlusterVolumeEntity aVolume : actual) {
            for(GlusterVolumeEntity eVolume : expected) {
                if(aVolume.getId().equals(eVolume.getId())) {
                    equals = true;
                    break;
                }
            }
            if(!equals) {
                break;
            }
        }
        return equals;
    }

    @Test
    public void testGetEligibleVolumeListQuery() {
        doReturn(new IdQueryParameters(baseTest.getMASTER_VOLUME_ID())).when(getQuery()).getParameters();
        getQuery().executeQueryCommand();
        List<GlusterVolumeEntity> returnValue = getQuery().getQueryReturnValue().getReturnValue();
        List<GlusterVolumeEntity> expectedVolumes = getExpectedVolumes();
        assertEquals(expectedVolumes.size(), returnValue.size());
        assertTrue(checkEquals(returnValue, expectedVolumes));
    }
}
