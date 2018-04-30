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

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.gluster.GlusterHookManageParameters;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookStatus;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.errors.VDSError;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDao;

@MockitoSettings(strictness = Strictness.LENIENT)
public class UpdateGlusterHookCommandTest extends GlusterHookCommandTest<UpdateGlusterHookCommand> {
    @Override
    protected UpdateGlusterHookCommand createCommand() {
        return new UpdateGlusterHookCommand(new GlusterHookManageParameters(HOOK_ID), null);
    }

    @Mock
    private VdsDao vdsDao;

    private static final String CONTENT = "TestContent";
    private static final Guid SERVER_ID = Guid.newGuid();

    private void setUpMocksForUpdate() {
        setUpMocksForUpdate(true);
    }

    private void setUpMocksForUpdate(boolean hookFound) {
        setUpMocksForUpdate(hookFound, getHookEntity());
    }

    private void setUpMocksForUpdate(boolean hookFound, GlusterHookEntity hook) {
        setUpMocksForUpdate(hookFound, hook, VDSStatus.Up);
    }

    private void setUpMocksForUpdate(VDSStatus status) {
        setUpMocksForUpdate(true, getHookEntity(), status);
    }

    private void setUpMocksForUpdate(boolean hookFound, GlusterHookEntity hook, VDSStatus status) {
        setupMocks(hookFound, hook);
        when(vdsDao.get(any())).thenReturn(getServer(SERVER_ID, "gfs1", CLUSTER_ID, status));
    }

    private void mockBackend(boolean succeeded, EngineError errorCode) {
        VDSReturnValue vdsReturnValue = new VDSReturnValue();
        vdsReturnValue.setSucceeded(succeeded);
        if (!succeeded) {
            vdsReturnValue.setVdsError(new VDSError(errorCode, ""));
        }
        when(vdsBrokerFrontend.runVdsCommand(eq(VDSCommandType.UpdateGlusterHook), any())).thenReturn(vdsReturnValue);
     }

    private void mockForReadContent(boolean succeeded, EngineError errorCode) {
        when(hooksDao.getGlusterServerHook(HOOK_ID, SERVER_ID)).thenReturn(getGlusterServerHook(0, GlusterHookStatus.ENABLED));
        mockBackend(succeeded, errorCode);
        VDSReturnValue vdsReturnValue = new VDSReturnValue();
        vdsReturnValue.setReturnValue(CONTENT);
        vdsReturnValue.setSucceeded(succeeded);
        if (!succeeded) {
            vdsReturnValue.setVdsError(new VDSError(errorCode, ""));
        }
        when(vdsBrokerFrontend.runVdsCommand(eq(VDSCommandType.GetGlusterHookContent), any())).thenReturn(vdsReturnValue);

    }

    @Test
    public void executeCommand() {
        setUpMocksForUpdate();
        mockBackend(true, null);
        cmd.executeCommand();
        verify(hooksDao, times(1)).updateGlusterHook(any());
        assertEquals(AuditLogType.GLUSTER_HOOK_UPDATED, cmd.getAuditLogTypeValue());
    }

    @Test
    public void executeCommandWhenServerIdPresent() {
        setUpMocksForUpdate();
        mockForReadContent(true, null);
        cmd.executeCommand();
        verify(hooksDao, times(1)).updateGlusterHook(any());
        assertEquals(AuditLogType.GLUSTER_HOOK_UPDATED, cmd.getAuditLogTypeValue());
    }

    @Test
    public void executeCommandWhenFailed() {
        setUpMocksForUpdate();
        mockBackend(false, EngineError.GlusterHookUpdateFailed);
        cmd.executeCommand();
        verify(hooksDao, never()).updateGlusterHook(any());
        assertEquals(AuditLogType.GLUSTER_HOOK_UPDATE_FAILED, cmd.getAuditLogTypeValue());
    }

    @Test
    public void executeCommandFailedWhenServerIdPresent() {
        setUpMocksForUpdate();
        mockForReadContent(false, EngineError.GlusterHookNotFound);
        try {
            cmd.executeCommand();
        }catch (EngineException e) {
            assertEquals(EngineError.GlusterHookNotFound, e.getErrorCode());
        }
        verify(hooksDao, never()).updateGlusterHook(any());
    }

    @Test
    public void validateSucceeds() {
        setUpMocksForUpdate();
        assertTrue(cmd.validate());
    }

    @Test
    public void validateFailsOnNullHookId() {
        cmd.getParameters().setHookId(null);
        setUpMocksForUpdate();
        assertFalse(cmd.validate());
        assertTrue(cmd.getReturnValue().getValidationMessages().contains(EngineMessage.ACTION_TYPE_FAILED_GLUSTER_HOOK_ID_IS_REQUIRED.toString()));
    }

    @Test
    public void validateFailsOnNoHook() {
        setUpMocksForUpdate(false);
        assertFalse(cmd.validate());
        assertTrue(cmd.getReturnValue().getValidationMessages().contains(EngineMessage.ACTION_TYPE_FAILED_GLUSTER_HOOK_DOES_NOT_EXIST.toString()));
    }

    @Test
    public void validateFailsOnNoConflictServers() {
        GlusterHookEntity hook = getHookEntity();
        hook.setServerHooks(Collections.singletonList(getGlusterServerHook(0, GlusterHookStatus.MISSING)));
        setUpMocksForUpdate(true, hook);
        assertFalse(cmd.validate());
        assertTrue(cmd.getReturnValue().getValidationMessages().contains(EngineMessage.ACTION_TYPE_FAILED_GLUSTER_HOOK_NO_CONFLICT_SERVERS.toString()));
    }

    @Test
    public void validateFailsOnServerNotUp() {
        setUpMocksForUpdate(VDSStatus.Down);
        assertFalse(cmd.validate());
        assertTrue(cmd.getReturnValue().getValidationMessages().contains(EngineMessage.ACTION_TYPE_FAILED_SERVER_STATUS_NOT_UP.toString()));
    }



}
