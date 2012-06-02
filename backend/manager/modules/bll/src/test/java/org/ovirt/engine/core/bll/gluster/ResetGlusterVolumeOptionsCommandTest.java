package org.ovirt.engine.core.bll.gluster;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ovirt.engine.core.bll.utils.ClusterUtils;
import org.ovirt.engine.core.common.action.gluster.ResetGlusterVolumeOptionsParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ DbFacade.class, GlusterVolumeDao.class, ResetGlusterVolumeOptionsCommand.class, ClusterUtils.class })
public class ResetGlusterVolumeOptionsCommandTest {
    @Mock
    DbFacade db;

    @Mock
    GlusterVolumeDao volumeDao;

    @Mock
    ClusterUtils clusterUtils;

    private ResetGlusterVolumeOptionsCommand cmd;

    private Guid CLUSTER_ID = new Guid("b399944a-81ab-4ec5-8266-e19ba7c3c9d1");

    private ResetGlusterVolumeOptionsCommand createTestCommand(Guid volumeId) {
        return new ResetGlusterVolumeOptionsCommand(new ResetGlusterVolumeOptionsParameters(volumeId, "", false));
    }

    @Before
    public void mockDbFacadeAndDao() {
        MockitoAnnotations.initMocks(this);
        mockStatic(DbFacade.class);
        mockStatic(GlusterVolumeDao.class);
        mockStatic(ClusterUtils.class);
        when(db.getGlusterVolumeDao()).thenReturn(volumeDao);
        when(DbFacade.getInstance()).thenReturn(db);
        when(volumeDao.getById(null)).thenReturn(null);
        when(ClusterUtils.getInstance()).thenReturn(clusterUtils);
        when(clusterUtils.getUpServer(CLUSTER_ID)).thenReturn(getVds(VDSStatus.Up));
    }

    private VDS getVds(VDSStatus status) {
        VDS vds = new VDS();
        vds.setId(Guid.NewGuid());
        vds.setvds_name("gfs1");
        vds.setvds_group_id(CLUSTER_ID);
        vds.setstatus(status);
        return vds;
    }

     @Test
    public void canDoActionFailsOnNull() {
        cmd = createTestCommand(null);
        assertFalse(cmd.canDoAction());
    }

}
