package org.ovirt.engine.core.bll.utils;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.gluster.PeerStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDao;

public class ClusterUtilsTest {

    private Guid CLUSTER_ID = new Guid("b399944a-81ab-4ec5-8266-e19ba7c3c9d1");
    VdsDao vdsDao;
    ClusterUtils clusterUtils;

    @Before
    public void mockDbFacadeAndDao() {
        vdsDao = mock(VdsDao.class);
        when(vdsDao.getAllForClusterWithStatusAndPeerStatus(CLUSTER_ID, VDSStatus.Up,
                PeerStatus.CONNECTED)).thenReturn(mockGetAllVdsForwithStatus(VDSStatus.Up));

        clusterUtils = spy(ClusterUtils.getInstance());
        doReturn(vdsDao).when(clusterUtils).getVdsDao();
    }

    private List<VDS> mockGetAllVdsForwithStatus(VDSStatus status) {
        List<VDS> vdsList = new ArrayList<>();
        vdsList.add(getVds(status));
        return vdsList;
    }

    private VDS getVds(VDSStatus status) {
        VDS vds = new VDS();
        vds.setId(Guid.Empty);
        vds.setVdsName("gfs1");
        vds.setClusterId(CLUSTER_ID);
        vds.setStatus(status);
        return vds;
    }

}
