package org.ovirt.engine.core.bll.gluster;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.gluster.GlusterHookStatusChangeParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerHook;
import org.ovirt.engine.core.dal.VdcBllMessages;


@RunWith(MockitoJUnitRunner.class)
public class EnableGlusterHookCommandTest extends GlusterHookCommandTest<EnableGlusterHookCommand> {
    /**
     * The command under test.
     */

    EnableGlusterHookCommand cmd;

    @Test
    public void executeCommand() {
        cmd = spy(new EnableGlusterHookCommand(new GlusterHookStatusChangeParameters(CLUSTER_ID, HOOK_ID)));
        setupMocks(cmd);
        mockBackendStatusChange(cmd,true);
        cmd.executeCommand();
        verify(cmd, atLeast(1)).addServerHookInDb(any(GlusterServerHook.class));
        assertEquals(cmd.getAuditLogTypeValue(), AuditLogType.GLUSTER_HOOK_ENABLE);
    }

    @Test
    public void executeCommandWhenFailed() {
        cmd = spy(new EnableGlusterHookCommand(new GlusterHookStatusChangeParameters(CLUSTER_ID, HOOK_ID)));
        setupMocks(cmd);
        mockBackendStatusChange(cmd,false);
        cmd.executeCommand();
        verify(cmd, never()).addServerHookInDb(any(GlusterServerHook.class));
        assertEquals(cmd.getAuditLogTypeValue(), AuditLogType.GLUSTER_HOOK_ENABLE_FAILED);
    }

    @Test
    public void canDoActionSucceeds() {
        cmd = spy(new EnableGlusterHookCommand(new GlusterHookStatusChangeParameters(CLUSTER_ID, HOOK_ID)));
        setupMocks(cmd);
        assertTrue(cmd.canDoAction());
    }

    @Test
    public void canDoActionFailsOnNullCluster() {
        cmd = spy(new EnableGlusterHookCommand(new GlusterHookStatusChangeParameters(null, HOOK_ID)));
        setupMocks(cmd);
        assertFalse(cmd.canDoAction());
        assertTrue(cmd.getReturnValue().getCanDoActionMessages().contains(VdcBllMessages.ACTION_TYPE_FAILED_CLUSTER_IS_NOT_VALID.toString()));
    }

    @Test
    public void canDoActionFailsOnNullHookId() {
        cmd = spy(new EnableGlusterHookCommand(new GlusterHookStatusChangeParameters(CLUSTER_ID, null)));
        setupMocks(cmd);
        assertFalse(cmd.canDoAction());
        assertTrue(cmd.getReturnValue().getCanDoActionMessages().contains(VdcBllMessages.ACTION_TYPE_FAILED_GLUSTER_HOOK_ID_IS_REQUIRED.toString()));
    }

    @Test
    public void canDoActionFailsOnNoHook() {
        cmd = spy(new EnableGlusterHookCommand(new GlusterHookStatusChangeParameters(CLUSTER_ID, HOOK_ID)));
        setupMocks(cmd,false);
        assertFalse(cmd.canDoAction());
        assertTrue(cmd.getReturnValue().getCanDoActionMessages().contains(VdcBllMessages.ACTION_TYPE_FAILED_GLUSTER_HOOK_DOES_NOT_EXIST.toString()));
    }

}
