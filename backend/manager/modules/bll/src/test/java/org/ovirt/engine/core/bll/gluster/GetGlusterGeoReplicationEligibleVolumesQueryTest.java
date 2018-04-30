package org.ovirt.engine.core.bll.gluster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.ovirt.engine.core.bll.AbstractQueryTest;
import org.ovirt.engine.core.bll.utils.GlusterGeoRepUtil;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSizeInfo;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.gluster.GlusterGeoRepDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;

public class GetGlusterGeoReplicationEligibleVolumesQueryTest extends AbstractQueryTest<IdQueryParameters, GetGlusterGeoReplicationEligibleVolumesQuery<IdQueryParameters>> {

    @Mock
    private GlusterGeoRepDao geoRepDao;

    @Mock
    private ClusterDao clusterDao;

    @Mock
    private GlusterVolumeDao volumeDao;

    @Spy
    @InjectMocks
    private GlusterGeoRepUtil geoRepUtil;

    private GeoRepCreateEligibilityBaseTest baseTest = new GeoRepCreateEligibilityBaseTest();

    @BeforeEach
    public void setupMock() {
        doReturn(geoRepUtil).when(getQuery()).getGeoRepUtilInstance();
        doReturn(Guid.newGuid()).when(geoRepUtil).getUpServerId(any());
        doReturn(true).when(geoRepUtil).checkEmptyGlusterVolume(any(), any());
        doReturn(getExpectedVolumes()).when(getQuery()).getAllGlusterVolumesWithMasterCompatibleVersion(baseTest.getMASTER_VOLUME_ID());
        baseTest.setupMock(geoRepDao, clusterDao);
        doReturn(baseTest.getMasterVolume()).when(volumeDao).getById(baseTest.getMASTER_VOLUME_ID());
    }

    private List<GlusterVolumeEntity> getExpectedVolumes() {
        return Collections.singletonList(baseTest.getGlusterVolume(baseTest.getSLAVE_VOLUME_1_ID(), baseTest.getSLAVE_CLUSTER_ID(), GlusterStatus.UP, new GlusterVolumeSizeInfo(10000L, 10000L, 0L)));
    }

    private boolean checkEquals(List<GlusterVolumeEntity> actual, List<GlusterVolumeEntity> expected) {
        Set<Guid> actualSet = actual.stream().map(BusinessEntity::getId).collect(Collectors.toSet());
        Set<Guid> expectedSet = expected.stream().map(BusinessEntity::getId).collect(Collectors.toSet());
        return actualSet.equals(expectedSet);
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
