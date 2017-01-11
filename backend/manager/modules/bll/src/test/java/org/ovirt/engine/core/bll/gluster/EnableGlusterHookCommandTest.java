package org.ovirt.engine.core.bll.gluster;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner.Silent;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.gluster.GlusterHookParameters;
import org.ovirt.engine.core.common.errors.EngineMessage;

@RunWith(Silent.class)
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
