package org.ovirt.engine.core.bll.gluster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.gluster.GlusterHookParameters;
import org.ovirt.engine.core.common.errors.EngineMessage;

@MockitoSettings(strictness = Strictness.LENIENT)
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
