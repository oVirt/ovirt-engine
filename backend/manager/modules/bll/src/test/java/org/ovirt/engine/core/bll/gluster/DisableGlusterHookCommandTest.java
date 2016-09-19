package org.ovirt.engine.core.bll.gluster;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.gluster.GlusterHookParameters;
import org.ovirt.engine.core.common.errors.EngineMessage;

@RunWith(MockitoJUnitRunner.class)
public class DisableGlusterHookCommandTest extends GlusterHookCommandTest<DisableGlusterHookCommand<GlusterHookParameters>> {
    @Override
    protected DisableGlusterHookCommand<GlusterHookParameters> createCommand() {
        return new DisableGlusterHookCommand<>(new GlusterHookParameters(HOOK_ID), null);
    }

    @Test
    public void validateSucceeds() {
        setupMocks();
        assertTrue(cmd.validate());
    }

    @Test
    public void validateFailsOnNullHookId() {
        setupMocks();
        cmd.getParameters().setHookId(null);
        assertFalse(cmd.validate());
        assertTrue(cmd.getReturnValue().getValidationMessages().contains(EngineMessage.ACTION_TYPE_FAILED_GLUSTER_HOOK_ID_IS_REQUIRED.toString()));

    }

    @Test
    public void validateFailsOnNullHook() {
        setupMocks(false);
        assertFalse(cmd.validate());
        assertTrue(cmd.getReturnValue().getValidationMessages().contains(EngineMessage.ACTION_TYPE_FAILED_GLUSTER_HOOK_DOES_NOT_EXIST.toString()));
    }

    @Test
    public void executeCommand() {
        setupMocks();
        mockBackendStatusChange(true);
        cmd.executeCommand();
        assertTrue(cmd.getReturnValue().getSucceeded());
        assertEquals(AuditLogType.GLUSTER_HOOK_DISABLE, cmd.getAuditLogTypeValue());
    }

    @Test
    public void executeCommandWhenFailed() {
        setupMocks();
        mockBackendStatusChange(false);
        cmd.executeCommand();
        assertFalse(cmd.getReturnValue().getSucceeded());
        assertEquals(AuditLogType.GLUSTER_HOOK_DISABLE_FAILED, cmd.getAuditLogTypeValue());
    }

}
