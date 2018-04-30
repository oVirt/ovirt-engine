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
public class EnableGlusterHookCommandTest extends GlusterHookCommandTest<EnableGlusterHookCommand<GlusterHookParameters>> {
    @Override
    protected EnableGlusterHookCommand<GlusterHookParameters> createCommand() {
        return new EnableGlusterHookCommand<>(new GlusterHookParameters(HOOK_ID), null);
    }

    @Test
    public void executeCommand() {
        setupMocks();
        mockBackendStatusChange(true);
        cmd.executeCommand();
        assertEquals(AuditLogType.GLUSTER_HOOK_ENABLE, cmd.getAuditLogTypeValue());
    }

    @Test
    public void executeCommandWhenFailed() {
        setupMocks();
        mockBackendStatusChange(false);
        cmd.executeCommand();
        assertEquals(AuditLogType.GLUSTER_HOOK_ENABLE_FAILED, cmd.getAuditLogTypeValue());
    }

    @Test
    public void validateSucceeds() {
        setupMocks();
        assertTrue(cmd.validate());
    }

    @Test
    public void validateFailsOnNullHookId() {
        cmd.getParameters().setHookId(null);
        setupMocks();
        assertFalse(cmd.validate());
        assertTrue(cmd.getReturnValue().getValidationMessages().contains(EngineMessage.ACTION_TYPE_FAILED_GLUSTER_HOOK_ID_IS_REQUIRED.toString()));
    }

    @Test
    public void validateFailsOnNoHook() {
        setupMocks(false);
        assertFalse(cmd.validate());
        assertTrue(cmd.getReturnValue().getValidationMessages().contains(EngineMessage.ACTION_TYPE_FAILED_GLUSTER_HOOK_DOES_NOT_EXIST.toString()));
    }

}
