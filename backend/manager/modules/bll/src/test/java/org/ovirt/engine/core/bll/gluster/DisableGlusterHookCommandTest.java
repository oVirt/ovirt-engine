package org.ovirt.engine.core.bll.gluster;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.gluster.GlusterHookParameters;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.dal.VdcBllMessages;

@RunWith(MockitoJUnitRunner.class)
public class DisableGlusterHookCommandTest extends GlusterHookCommandTest<DisableGlusterHookCommand> {

     /**
     * The command under test.
     */
    @Mock
    private DisableGlusterHookCommand cmd;

     @Test
    public void canDoActionSucceeds() {
        cmd = spy(new DisableGlusterHookCommand(new GlusterHookParameters(CLUSTER_ID, HOOK_ID)));
        setupMocks(cmd);
        assertTrue(cmd.canDoAction());
    }

    @Test
    public void canDoActionFailsOnNullCluster() {
        cmd = spy(new DisableGlusterHookCommand(new GlusterHookParameters(null, HOOK_ID)));
        setupMocks(cmd);
        assertFalse(cmd.canDoAction());
        assertTrue(cmd.getReturnValue().getCanDoActionMessages().contains(VdcBllMessages.ACTION_TYPE_FAILED_CLUSTER_IS_NOT_VALID.toString()));
    }

    @Test
    public void canDoActionFailsOnNullHookId() {
        cmd = spy(new DisableGlusterHookCommand(new GlusterHookParameters(CLUSTER_ID, null)));
        setupMocks(cmd);
        assertFalse(cmd.canDoAction());
        assertTrue(cmd.getReturnValue().getCanDoActionMessages().contains(VdcBllMessages.ACTION_TYPE_FAILED_GLUSTER_HOOK_ID_IS_REQUIRED.toString()));

    }

    @Test
    public void canDoActionFailsOnNullHook() {
        cmd = spy(new DisableGlusterHookCommand(new GlusterHookParameters(CLUSTER_ID, HOOK_ID)));
        setupMocks(cmd,false);
        assertFalse(cmd.canDoAction());
        assertTrue(cmd.getReturnValue().getCanDoActionMessages().contains(VdcBllMessages.ACTION_TYPE_FAILED_GLUSTER_HOOK_DOES_NOT_EXIST.toString()));
    }

    @Test
    public void executeCommand() {
        cmd = spy(new DisableGlusterHookCommand(new GlusterHookParameters(CLUSTER_ID, HOOK_ID)));
        setupMocks(cmd);
        mockBackend(cmd,true);
        cmd.executeCommand();
        assertTrue(cmd.getReturnValue().getSucceeded());
        assertEquals(cmd.getAuditLogTypeValue(), AuditLogType.GLUSTER_HOOK_DISABLE);
    }

    @Test
    public void executeCommandWhenFailed() {
        cmd = spy(new DisableGlusterHookCommand(new GlusterHookParameters(CLUSTER_ID, HOOK_ID)));
        setupMocks(cmd);
        mockBackend(cmd,false);
        cmd.executeCommand();
        assertFalse(cmd.getReturnValue().getSucceeded());
        assertEquals(cmd.getAuditLogTypeValue(), AuditLogType.GLUSTER_HOOK_DISABLE_FAILED);
    }

    @Test
    public void executeCommandWhenAlreadyDisabled() {
        cmd = spy(new DisableGlusterHookCommand(new GlusterHookParameters(CLUSTER_ID, HOOK_ID)));
        setupMocks(cmd);
        mockBackend(cmd,false,VdcBllErrors.GlusterHookAlreadyDisabled);
        cmd.executeCommand();
        assertTrue(cmd.getReturnValue().getSucceeded());
        assertEquals(cmd.getAuditLogTypeValue(), AuditLogType.GLUSTER_HOOK_DISABLE);
    }

}
