package org.ovirt.engine.core.bll.gluster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
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
    @Spy
    @InjectMocks
    RefreshGlusterHooksCommand<GlusterClusterParameters> cmd =
            new RefreshGlusterHooksCommand<>(new GlusterClusterParameters(CLUSTER_ID), null);

    @Mock
    private ClusterDao clusterDao;

    @Mock
    private GlusterHookSyncJob hookSyncJob;

    public void setupMocks() {
        when(clusterDao.get(CLUSTER_ID)).thenReturn(getCluster());
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
        setupMocks();
        cmd.executeCommand();
        assertEquals(AuditLogType.GLUSTER_HOOK_REFRESH, cmd.getAuditLogTypeValue());
    }

    @Test
    public void executeCommandWhenFailed() {
        setupMocks();
        doThrow(new EngineException(EngineError.GlusterHookListException)).when(hookSyncJob).refreshHooksInCluster(getCluster(), true);
        EngineException e = assertThrows(EngineException.class, cmd::executeCommand);
        assertEquals(EngineError.GlusterHookListException, e.getErrorCode());
        assertEquals(AuditLogType.GLUSTER_HOOK_REFRESH_FAILED, cmd.getAuditLogTypeValue());
    }

    @Test
    public void validateSucceeds() {
        setupMocks();
        doReturn(getServer()).when(cmd).getUpServer();
        assertTrue(cmd.validate());
    }

    @Test
    public void validateFailsOnNullCluster() {
        cmd.setClusterId(null);
        assertFalse(cmd.validate());
        assertTrue(cmd.getReturnValue().getValidationMessages().contains(EngineMessage.ACTION_TYPE_FAILED_CLUSTER_IS_NOT_VALID.toString()));
    }

}
