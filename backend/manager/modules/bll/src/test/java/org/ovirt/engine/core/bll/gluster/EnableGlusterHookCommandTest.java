package org.ovirt.engine.core.bll.gluster;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.gluster.GlusterHookParameters;
import org.ovirt.engine.core.common.errors.EngineMessage;

@RunWith(MockitoJUnitRunner.class)
public class EnableGlusterHookCommandTest extends GlusterHookCommandTest<EnableGlusterHookCommand<GlusterHookParameters>> {
    /**
     * The command under test.
     */

    EnableGlusterHookCommand<GlusterHookParameters> cmd;

    @Test
    public void executeCommand() {
        cmd = spy(new EnableGlusterHookCommand<>(new GlusterHookParameters(HOOK_ID), null));
        setupMocks(cmd);
        mockBackendStatusChange(cmd, true);
        cmd.executeCommand();
        assertEquals(cmd.getAuditLogTypeValue(), AuditLogType.GLUSTER_HOOK_ENABLE);
    }

    @Test
    public void executeCommandWhenFailed() {
        cmd = spy(new EnableGlusterHookCommand<>(new GlusterHookParameters(HOOK_ID), null));
        setupMocks(cmd);
        mockBackendStatusChange(cmd, false);
        cmd.executeCommand();
        assertEquals(cmd.getAuditLogTypeValue(), AuditLogType.GLUSTER_HOOK_ENABLE_FAILED);
    }

    @Test
    public void validateSucceeds() {
        cmd = spy(new EnableGlusterHookCommand<>(new GlusterHookParameters(HOOK_ID), null));
        setupMocks(cmd);
        assertTrue(cmd.validate());
    }

    @Test
    public void validateFailsOnNullHookId() {
        cmd = spy(new EnableGlusterHookCommand<>(new GlusterHookParameters(null), null));
        setupMocks(cmd);
        assertFalse(cmd.validate());
        assertTrue(cmd.getReturnValue().getValidationMessages().contains(EngineMessage.ACTION_TYPE_FAILED_GLUSTER_HOOK_ID_IS_REQUIRED.toString()));
    }

    @Test
    public void validateFailsOnNoHook() {
        cmd = spy(new EnableGlusterHookCommand<>(new GlusterHookParameters(HOOK_ID), null));
        setupMocks(cmd, false);
        assertFalse(cmd.validate());
        assertTrue(cmd.getReturnValue().getValidationMessages().contains(EngineMessage.ACTION_TYPE_FAILED_GLUSTER_HOOK_DOES_NOT_EXIST.toString()));
    }

}
