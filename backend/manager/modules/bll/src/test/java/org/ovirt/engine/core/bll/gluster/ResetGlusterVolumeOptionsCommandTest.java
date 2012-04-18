package org.ovirt.engine.core.bll.gluster;

import static org.junit.Assert.assertFalse;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ovirt.engine.core.common.action.gluster.ResetGlusterVolumeOptionsParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ DbFacade.class, GlusterVolumeDao.class, ResetGlusterVolumeOptionsCommand.class })
public class ResetGlusterVolumeOptionsCommandTest {
    @Mock
    DbFacade db;

    @Mock
    GlusterVolumeDao volumeDao;

    private ResetGlusterVolumeOptionsCommand cmd;

    private ResetGlusterVolumeOptionsCommand createTestCommand(Guid volumeId) {
        return new ResetGlusterVolumeOptionsCommand(new ResetGlusterVolumeOptionsParameters(volumeId, "", false));
    }

    @Before
    public void mockDbFacadeAndDao() {
        MockitoAnnotations.initMocks(this);
        mockStatic(DbFacade.class);
        mockStatic(GlusterVolumeDao.class);
        when(db.getGlusterVolumeDao()).thenReturn(volumeDao);
        when(DbFacade.getInstance()).thenReturn(db);
        when(volumeDao.getById(null)).thenReturn(null);
    }

     @Test
    public void canDoActionFailsOnNull() {
        cmd = createTestCommand(null);
        assertFalse(cmd.canDoAction());
    }

}
