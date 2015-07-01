package org.ovirt.engine.core.bll.gluster;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.gluster.GlusterClusterParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsGroupDao;


@RunWith(MockitoJUnitRunner.class)
public class RefreshGlusterHooksCommandTest {
    private static final Guid CLUSTER_ID = Guid.newGuid();
    /**
     * The command under test.
     */

    RefreshGlusterHooksCommand<GlusterClusterParameters> cmd;

    @Mock
    private VdsGroupDao vdsGroupDao;

    @Mock
    private GlusterHookSyncJob hookSyncJob;

    public void setupMocks() {
        when(vdsGroupDao.get(CLUSTER_ID)).thenReturn(getVdsGroup());
        doReturn(vdsGroupDao).when(cmd).getVdsGroupDao();
        when(cmd.getSyncJobInstance()).thenReturn(hookSyncJob);
    }

    private VDSGroup getVdsGroup() {
        VDSGroup cluster = new VDSGroup();
        cluster.setId(CLUSTER_ID);
        cluster.setName("TestCluster");
        return cluster;
    }

    protected VDS getServer() {
        VDS server =  new VDS();
        server.setId(Guid.newGuid());
        server.setVdsName("VDS1");
        server.setStatus(VDSStatus.Up);
        server.setVdsGroupId(CLUSTER_ID);
        return server;
    }

    @Test
    public void executeCommand() {
        cmd = spy(new RefreshGlusterHooksCommand<GlusterClusterParameters>(new GlusterClusterParameters(CLUSTER_ID)));
        setupMocks();
        doNothing().when(hookSyncJob).refreshHooksInCluster(getVdsGroup(), true);
        cmd.executeCommand();
        assertEquals(cmd.getAuditLogTypeValue(), AuditLogType.GLUSTER_HOOK_REFRESH);
    }

    @Test
    public void executeCommandWhenFailed() {
        cmd = spy(new RefreshGlusterHooksCommand<GlusterClusterParameters>(new GlusterClusterParameters(CLUSTER_ID)));
        setupMocks();
        doThrow(new VdcBLLException(VdcBllErrors.GlusterHookListException)).when(hookSyncJob).refreshHooksInCluster(getVdsGroup(), true);
        try {
            cmd.executeCommand();
            fail("Expected VdcBLLException");
        } catch (VdcBLLException e) {
            assertEquals(e.getErrorCode(), VdcBllErrors.GlusterHookListException);
            assertEquals(cmd.getAuditLogTypeValue(), AuditLogType.GLUSTER_HOOK_REFRESH_FAILED);
        }
    }

    @Test
    public void canDoActionSucceeds() {
        cmd = spy(new RefreshGlusterHooksCommand<GlusterClusterParameters>(new GlusterClusterParameters(CLUSTER_ID)));
        setupMocks();
        doReturn(getServer()).when(cmd).getUpServer();
        assertTrue(cmd.canDoAction());
    }

    @Test
    public void canDoActionFailsOnNullCluster() {
        cmd = spy(new RefreshGlusterHooksCommand<GlusterClusterParameters>(new GlusterClusterParameters(null)));
        assertFalse(cmd.canDoAction());
        assertTrue(cmd.getReturnValue().getCanDoActionMessages().contains(VdcBllMessages.ACTION_TYPE_FAILED_CLUSTER_IS_NOT_VALID.toString()));
    }

}
