package org.ovirt.engine.core.bll.gluster;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
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
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterHookVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDao;


@RunWith(MockitoJUnitRunner.class)
public class UpdateGlusterHookCommandTest extends GlusterHookCommandTest<UpdateGlusterHookCommand> {
    /**
     * The command under test.
     */
    UpdateGlusterHookCommand cmd;

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
        setupMocks(cmd, hookFound, hook);
        doReturn(vdsDao).when(cmd).getVdsDao();
        when(vdsDao.get(any(Guid.class))).thenReturn(getServer(SERVER_ID, "gfs1", CLUSTER_ID, status));
    }

    private void mockBackend(boolean succeeded, EngineError errorCode) {
        doReturn(backend).when(cmd).getBackend();

        VDSReturnValue vdsReturnValue = new VDSReturnValue();
        vdsReturnValue.setSucceeded(succeeded);
        if (!succeeded) {
            vdsReturnValue.setVdsError(new VDSError(errorCode, ""));
        }
        when(vdsBrokerFrontend.runVdsCommand(eq(VDSCommandType.UpdateGlusterHook), argThat(anyHookVDS()))).thenReturn(vdsReturnValue);
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
        when(vdsBrokerFrontend.runVdsCommand(eq(VDSCommandType.GetGlusterHookContent), argThat(anyHookVDS()))).thenReturn(vdsReturnValue);

    }

    private ArgumentMatcher<VDSParametersBase> anyHookVDS() {
        return new ArgumentMatcher<VDSParametersBase>() {

            @Override
            public boolean matches(Object argument) {
                if (!(argument instanceof GlusterHookVDSParameters)) {
                    return false;
                }
                return true;
            }
        };
    }

    @Test
    public void executeCommand() {
        cmd = spy(new UpdateGlusterHookCommand(new GlusterHookManageParameters(HOOK_ID), null));
        setUpMocksForUpdate();
        mockBackend(true, null);
        cmd.executeCommand();
        verify(hooksDao, times(1)).updateGlusterHook(any(GlusterHookEntity.class));
        assertEquals(cmd.getAuditLogTypeValue(), AuditLogType.GLUSTER_HOOK_UPDATED);
    }

    @Test
    public void executeCommandWhenServerIdPresent() {
        cmd = spy(new UpdateGlusterHookCommand(new GlusterHookManageParameters(HOOK_ID, SERVER_ID), null));
        setUpMocksForUpdate();
        mockForReadContent(true, null);
        cmd.executeCommand();
        verify(hooksDao, times(1)).updateGlusterHook(any(GlusterHookEntity.class));
        assertEquals(cmd.getAuditLogTypeValue(), AuditLogType.GLUSTER_HOOK_UPDATED);
    }

    @Test
    public void executeCommandWhenFailed() {
        cmd = spy(new UpdateGlusterHookCommand(new GlusterHookManageParameters(HOOK_ID), null));
        setUpMocksForUpdate();
        mockBackend(false, EngineError.GlusterHookUpdateFailed);
        cmd.executeCommand();
        verify(hooksDao, never()).updateGlusterHook(any(GlusterHookEntity.class));
        assertEquals(cmd.getAuditLogTypeValue(), AuditLogType.GLUSTER_HOOK_UPDATE_FAILED);
    }

    @Test
    public void executeCommandFailedWhenServerIdPresent() {
        cmd = spy(new UpdateGlusterHookCommand(new GlusterHookManageParameters(HOOK_ID, SERVER_ID), null));
        setUpMocksForUpdate();
        mockForReadContent(false, EngineError.GlusterHookNotFound);
        try {
            cmd.executeCommand();
        }catch (EngineException e) {
            assertEquals(e.getErrorCode(), EngineError.GlusterHookNotFound);
        }
        verify(hooksDao, never()).updateGlusterHook(any(GlusterHookEntity.class));
    }

    @Test
    public void validateSucceeds() {
        cmd = spy(new UpdateGlusterHookCommand(new GlusterHookManageParameters(HOOK_ID), null));
        setUpMocksForUpdate();
        assertTrue(cmd.validate());
    }

    @Test
    public void validateFailsOnNullHookId() {
        cmd = spy(new UpdateGlusterHookCommand(new GlusterHookManageParameters(null), null));
        setUpMocksForUpdate();
        assertFalse(cmd.validate());
        assertTrue(cmd.getReturnValue().getValidationMessages().contains(EngineMessage.ACTION_TYPE_FAILED_GLUSTER_HOOK_ID_IS_REQUIRED.toString()));
    }

    @Test
    public void validateFailsOnNoHook() {
        cmd = spy(new UpdateGlusterHookCommand(new GlusterHookManageParameters(HOOK_ID), null));
        setUpMocksForUpdate(false);
        assertFalse(cmd.validate());
        assertTrue(cmd.getReturnValue().getValidationMessages().contains(EngineMessage.ACTION_TYPE_FAILED_GLUSTER_HOOK_DOES_NOT_EXIST.toString()));
    }

    @Test
    public void validateFailsOnNoConflictServers() {
        cmd = spy(new UpdateGlusterHookCommand(new GlusterHookManageParameters(HOOK_ID), null));
        GlusterHookEntity hook = getHookEntity();
        hook.setServerHooks(Collections.singletonList(getGlusterServerHook(0, GlusterHookStatus.MISSING)));
        setUpMocksForUpdate(true, hook);
        assertFalse(cmd.validate());
        assertTrue(cmd.getReturnValue().getValidationMessages().contains(EngineMessage.ACTION_TYPE_FAILED_GLUSTER_HOOK_NO_CONFLICT_SERVERS.toString()));
    }

    @Test
    public void validateFailsOnServerNotUp() {
        cmd = spy(new UpdateGlusterHookCommand(new GlusterHookManageParameters(HOOK_ID), null));
        setUpMocksForUpdate(VDSStatus.Down);
        assertFalse(cmd.validate());
        assertTrue(cmd.getReturnValue().getValidationMessages().contains(EngineMessage.ACTION_TYPE_FAILED_SERVER_STATUS_NOT_UP.toString()));
    }



}
