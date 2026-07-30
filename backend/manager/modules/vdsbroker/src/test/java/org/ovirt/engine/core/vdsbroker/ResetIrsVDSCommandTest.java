package org.ovirt.engine.core.vdsbroker;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.vdscommands.ResetIrsVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.SpmStopVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.vdsbroker.irsbroker.IrsProxy;
import org.ovirt.engine.core.vdsbroker.irsbroker.IrsProxyManager;
import org.ovirt.engine.core.vdsbroker.irsbroker.ResetIrsVDSCommand;

/**
 * Regression tests pinning the semantics the SPM lease guard in
 * ReconstructMasterDomainCommand relies on: the ResetIrs it issues with
 * ignoreStopFailed=true always ends in IrsProxy.resetIrs() (which clears
 * spm_vds_id in the DB) even though the command reports success without
 * having stopped the SPM. This is what makes the lease guard one-shot per
 * SPM episode: a refused reconstruct leaves spm_vds_id null, so the next
 * reconstruct attempt skips the guard entirely.
 */
@ExtendWith(MockitoExtension.class)
public class ResetIrsVDSCommandTest {

    private Guid storagePoolId = Guid.newGuid();

    private Guid spmVdsId = Guid.newGuid();

    @Mock
    private ResourceManager resourceManager;

    @Mock
    private IrsProxyManager irsProxyManager;

    @Mock
    private IrsProxy irsProxy;

    @Mock
    private StoragePoolDao storagePoolDao;

    private ResetIrsVDSCommandParameters parameters =
            new ResetIrsVDSCommandParameters(storagePoolId, spmVdsId);

    @InjectMocks
    private ResetIrsVDSCommand<ResetIrsVDSCommandParameters> cmd =
            new ResetIrsVDSCommand<>(parameters);

    /**
     * The exact combination the lease guard issues: SpmStop on the recorded
     * SPM fails (host unreachable), but ignoreStopFailed=true must still
     * carry the command through the success branch and resetIrs() -- clearing
     * spm_vds_id in the DB -- while reporting success.
     */
    @Test
    public void testIgnoreStopFailedResetsIrsDespiteSpmStopFailure() {
        parameters.setIgnoreStopFailed(true);
        when(resourceManager.runVdsCommand(eq(VDSCommandType.SpmStop),
                any(SpmStopVDSCommandParameters.class))).thenReturn(failedReturn());
        when(irsProxyManager.getCurrentProxy(storagePoolId)).thenReturn(irsProxy);
        when(storagePoolDao.get(storagePoolId)).thenReturn(poolInStatus(StoragePoolStatus.NonResponsive));

        cmd.executeCommand();

        assertTrue(cmd.getVDSReturnValue().getSucceeded());
        verify(irsProxy).resetIrs();
        // not a fence: no fencedIrs must be recorded on this path
        verify(irsProxy, never()).setFencedIrs(any());
    }

    /**
     * When SpmStop genuinely succeeds, the command also ends in resetIrs().
     */
    @Test
    public void testSpmStopSuccessResetsIrs() {
        when(resourceManager.runVdsCommand(eq(VDSCommandType.SpmStop),
                any(SpmStopVDSCommandParameters.class))).thenReturn(successReturn());
        when(irsProxyManager.getCurrentProxy(storagePoolId)).thenReturn(irsProxy);
        when(storagePoolDao.get(storagePoolId)).thenReturn(poolInStatus(StoragePoolStatus.NonResponsive));

        cmd.executeCommand();

        assertTrue(cmd.getVDSReturnValue().getSucceeded());
        verify(irsProxy).resetIrs();
    }

    /**
     * Without ignoreStopFailed, a failed SpmStop must fail the command and
     * NOT resetIrs() (leaving spm_vds_id intact) -- proving resetIrs() runs
     * only on the success branch.
     */
    @Test
    public void testSpmStopFailureWithoutIgnoreStopFailedFailsCommand() {
        when(resourceManager.runVdsCommand(eq(VDSCommandType.SpmStop),
                any(SpmStopVDSCommandParameters.class))).thenReturn(failedReturn());

        cmd.executeCommand();

        assertFalse(cmd.getVDSReturnValue().getSucceeded());
        verify(irsProxy, never()).resetIrs();
    }

    /**
     * vdsAlreadyRebooted (out-of-band fence confirmation) short-circuits
     * SpmStop entirely: records fencedIrs, then resetsIrs().
     */
    @Test
    public void testVdsAlreadyRebootedSetsFencedIrsWithoutSpmStop() {
        parameters.setVdsAlreadyRebooted(true);
        when(irsProxyManager.getCurrentProxy(storagePoolId)).thenReturn(irsProxy);
        when(storagePoolDao.get(storagePoolId)).thenReturn(poolInStatus(StoragePoolStatus.NonResponsive));

        cmd.executeCommand();

        assertTrue(cmd.getVDSReturnValue().getSucceeded());
        verify(resourceManager, never()).runVdsCommand(any(), any());
        verify(irsProxy).setFencedIrs(spmVdsId);
        verify(irsProxy).resetIrs();
    }

    private StoragePool poolInStatus(StoragePoolStatus status) {
        StoragePool pool = new StoragePool();
        pool.setId(storagePoolId);
        pool.setStatus(status);
        return pool;
    }

    private static VDSReturnValue successReturn() {
        VDSReturnValue ret = new VDSReturnValue();
        ret.setSucceeded(true);
        return ret;
    }

    private static VDSReturnValue failedReturn() {
        VDSReturnValue ret = new VDSReturnValue();
        ret.setSucceeded(false);
        return ret;
    }
}
