package org.ovirt.engine.core.bll.gluster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.gluster.GlusterHookManageParameters;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerHook;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.errors.VDSError;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDao;

@MockitoSettings(strictness = Strictness.LENIENT)
public class AddGlusterHookCommandTest extends GlusterHookCommandTest<AddGlusterHookCommand<GlusterHookManageParameters>> {
    @Mock
    private VdsDao vdsDao;

    private static final Guid SERVER_ID = Guid.newGuid();

    @Override
    protected AddGlusterHookCommand<GlusterHookManageParameters> createCommand() {
        return new AddGlusterHookCommand<>(new GlusterHookManageParameters(HOOK_ID), null);
    }

    private void setUpMocksForAdd() {
        setUpMocksForAdd(true);
    }

    private void setUpMocksForAdd(boolean hookFound) {
        setUpMocksForAdd(hookFound, getHookEntityWithMissing());
    }

    private void setUpMocksForAdd(boolean hookFound, GlusterHookEntity hook) {
        setUpMocksForAdd(hookFound, hook, VDSStatus.Up);
    }

    private void setUpMocksForAdd(VDSStatus status) {
        setUpMocksForAdd(true, getHookEntityWithMissing(), status);
    }

    private GlusterHookEntity getHookEntityWithMissing() {
        GlusterHookEntity hook = getHookEntity();
        List<GlusterServerHook> serverHooks = new ArrayList<>();
        serverHooks.add(getGlusterServerHook(0, GlusterHookStatus.MISSING));
        serverHooks.add(getGlusterServerHook(1, GlusterHookStatus.MISSING));
        serverHooks.add(getGlusterServerHook(2, GlusterHookStatus.MISSING));
        hook.setServerHooks(serverHooks);
        return hook;
    }

    private void setUpMocksForAdd(boolean hookFound, GlusterHookEntity hook, VDSStatus status) {
        setupMocks(hookFound, hook);
        when(vdsDao.get(any())).thenReturn(getServer(SERVER_ID, "gfs1", CLUSTER_ID, status));
    }

    private void mockBackend(boolean succeeded, EngineError errorCode) {
        VDSReturnValue vdsReturnValue = new VDSReturnValue();
        vdsReturnValue.setSucceeded(succeeded);
        if (!succeeded) {
            vdsReturnValue.setVdsError(new VDSError(errorCode, ""));
        }
        when(vdsBrokerFrontend.runVdsCommand(eq(VDSCommandType.AddGlusterHook), any())).thenReturn(vdsReturnValue);
    }

    @Test
    public void executeCommand() {
        setUpMocksForAdd();
        mockBackend(true, null);
        cmd.executeCommand();
        verify(hooksDao, times(1)).updateGlusterHook(any());
        verify(hooksDao, times(3)).removeGlusterServerHook(any(), any());
        assertEquals(AuditLogType.GLUSTER_HOOK_ADDED, cmd.getAuditLogTypeValue());
    }


    @Test
    public void executeCommandWhenFailed() {
        setUpMocksForAdd();
        mockBackend(false, EngineError.GlusterHookAddFailed);
        cmd.executeCommand();
        verify(hooksDao, never()).updateGlusterHook(any());
        verify(hooksDao, never()).removeGlusterServerHook(any(), any());
        assertEquals(AuditLogType.GLUSTER_HOOK_ADD_FAILED, cmd.getAuditLogTypeValue());
    }

    @Test
    public void validateSucceeds() {
        setUpMocksForAdd();
        assertTrue(cmd.validate());
    }

    @Test
    public void validateFailsOnNullHookId() {
        cmd.getParameters().setHookId(null);
        setUpMocksForAdd();
        assertFalse(cmd.validate());
        assertTrue(cmd.getReturnValue().getValidationMessages().contains(EngineMessage.ACTION_TYPE_FAILED_GLUSTER_HOOK_ID_IS_REQUIRED.toString()));
    }

    @Test
    public void validateFailsOnNoHook() {
        setUpMocksForAdd(false);
        assertFalse(cmd.validate());
        assertTrue(cmd.getReturnValue().getValidationMessages().contains(EngineMessage.ACTION_TYPE_FAILED_GLUSTER_HOOK_DOES_NOT_EXIST.toString()));
    }

    @Test
    public void validateFailsOnNoConflictServers() {
        GlusterHookEntity hook = getHookEntity();
        setUpMocksForAdd(true, hook);
        assertFalse(cmd.validate());
        assertTrue(cmd.getReturnValue().getValidationMessages().contains(EngineMessage.ACTION_TYPE_FAILED_GLUSTER_HOOK_NO_CONFLICT_SERVERS.toString()));
    }

    @Test
    public void validateFailsOnServerNotUp() {
        setUpMocksForAdd(VDSStatus.Down);
        assertFalse(cmd.validate());
        assertTrue(cmd.getReturnValue().getValidationMessages().contains(EngineMessage.ACTION_TYPE_FAILED_SERVER_STATUS_NOT_UP.toString()));
    }
}
