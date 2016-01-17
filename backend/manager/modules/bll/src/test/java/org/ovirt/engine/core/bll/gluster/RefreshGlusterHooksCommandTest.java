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
import org.mockito.Mock;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.gluster.GlusterClusterParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ClusterDao;

public class RefreshGlusterHooksCommandTest extends BaseCommandTest {
    private static final Guid CLUSTER_ID = Guid.newGuid();
    /**
     * The command under test.
     */

    RefreshGlusterHooksCommand<GlusterClusterParameters> cmd;

    @Mock
    private ClusterDao clusterDao;

    @Mock
    private GlusterHookSyncJob hookSyncJob;

    public void setupMocks() {
        when(clusterDao.get(CLUSTER_ID)).thenReturn(getCluster());
        doReturn(clusterDao).when(cmd).getClusterDao();
        when(cmd.getSyncJobInstance()).thenReturn(hookSyncJob);
    }

    private Cluster getCluster() {
        Cluster cluster = new Cluster();
        cluster.setId(CLUSTER_ID);
        cluster.setName("TestCluster");
        return cluster;
    }

    protected VDS getServer() {
        VDS server =  new VDS();
        server.setId(Guid.newGuid());
        server.setVdsName("VDS1");
        server.setStatus(VDSStatus.Up);
        server.setClusterId(CLUSTER_ID);
        return server;
    }

    @Test
    public void executeCommand() {
        cmd = spy(new RefreshGlusterHooksCommand<>(new GlusterClusterParameters(CLUSTER_ID), null));
        setupMocks();
        doNothing().when(hookSyncJob).refreshHooksInCluster(getCluster(), true);
        cmd.executeCommand();
        assertEquals(cmd.getAuditLogTypeValue(), AuditLogType.GLUSTER_HOOK_REFRESH);
    }

    @Test
    public void executeCommandWhenFailed() {
        cmd = spy(new RefreshGlusterHooksCommand<>(new GlusterClusterParameters(CLUSTER_ID), null));
        setupMocks();
        doThrow(new EngineException(EngineError.GlusterHookListException)).when(hookSyncJob).refreshHooksInCluster(getCluster(), true);
        try {
            cmd.executeCommand();
            fail("Expected EngineException");
        } catch (EngineException e) {
            assertEquals(e.getErrorCode(), EngineError.GlusterHookListException);
            assertEquals(cmd.getAuditLogTypeValue(), AuditLogType.GLUSTER_HOOK_REFRESH_FAILED);
        }
    }

    @Test
    public void validateSucceeds() {
        cmd = spy(new RefreshGlusterHooksCommand<>(new GlusterClusterParameters(CLUSTER_ID), null));
        setupMocks();
        doReturn(getServer()).when(cmd).getUpServer();
        assertTrue(cmd.validate());
    }

    @Test
    public void validateFailsOnNullCluster() {
        cmd = spy(new RefreshGlusterHooksCommand<>(new GlusterClusterParameters(null), null));
        assertFalse(cmd.validate());
        assertTrue(cmd.getReturnValue().getValidationMessages().contains(EngineMessage.ACTION_TYPE_FAILED_CLUSTER_IS_NOT_VALID.toString()));
    }

}
