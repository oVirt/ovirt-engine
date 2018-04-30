package org.ovirt.engine.core.bll.network.vm;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.businessentities.VMStatus;

@MockitoSettings(strictness = Strictness.LENIENT)
public class MacIsNotReservedInSnapshotAndCanBeReleasedTest extends AllocateReleaseMacWhenBeingReservedForSnapshotTest {
    @Test
    public void testMacPoolCleanupAfterExecutionWhenMacWasntAddedAndExecutionFailed() {
        underTest.getReturnValue().setSucceeded(false);
        underTest.macPoolCleanupAfterExecution(false);
        verifyNoMoreInteractions(macPool);
    }

    @Test
    public void testMacPoolCleanupAfterExecutionWhenMacWasAddedButExecutionFailed() {
        nicBeingUpdated.setMacAddress(OLD_MAC);
        updatingNic.setMacAddress(NEW_MAC);

        underTest.initMacPoolData();

        //simulate command failed.
        underTest.getReturnValue().setSucceeded(false);
        underTest.macPoolCleanupAfterExecution(true);

        //MAC was added into pool, and command failed so it must be released.
        verify(macPool).freeMac(NEW_MAC);
    }

    @Test
    public void testMacPoolCleanupNonStatelessVmWhenMacWasUpdated() {
        nicBeingUpdated.setMacAddress(OLD_MAC);
        updatingNic.setMacAddress(NEW_MAC);

        underTest.initMacPoolData();

        //simulate command succeeded.
        underTest.getReturnValue().setSucceeded(true);
        underTest.macPoolCleanupAfterExecution(true);

        //there's no snapshot, update of mac address succeeded, so old value has to be released.
        verify(macPool).freeMac(OLD_MAC);
    }

    @Test
    public void testMacPoolCleanupForStatelessVmAfterDivergingFromSnapshot() {
        //we updated nic with new mac address, old mac address is equal to one stored in snapshot, and because it's in
        //snapshot, it must not be released.
        nicBeingUpdated.setMacAddress(OLD_MAC);
        updatingNic.setMacAddress(NEW_MAC);
        mockOriginalSnapshot(OLD_MAC, OTHER_MAC);

        setVmAsRunningStateless();
        underTest.initMacPoolData();

        //simulate command succeeded.
        underTest.getReturnValue().setSucceeded(true);
        underTest.macPoolCleanupAfterExecution(true);

        verifyNoMoreInteractions(macPool);
    }

    @Test
    public void testMacPoolCleanupForStatelessVmAfterReturningToStateInSnapshot() {
        /*
         * we did update when nic was snapshotted while having old_mac. Then it was updated with new_mac, and then we
         * updated it back again so that the nic have again the former mac, which is also stored in snapshot. Now we are
         * about to release mac value, from which we are reverting back to state in snapshot. This mac is not used in
         * snapshot, was related only to stateless vm, thus should be released.
         */
        nicBeingUpdated.setMacAddress(NEW_MAC);
        updatingNic.setMacAddress(OLD_MAC);
        mockOriginalSnapshot(OLD_MAC, OTHER_MAC);

        setVmAsRunningStateless();
        underTest.initMacPoolData();

        //simulate command succeeded.
        underTest.getReturnValue().setSucceeded(true);
        underTest.macPoolCleanupAfterExecution(false);

        verify(macPool).freeMac(NEW_MAC);
    }

    @Test
    public void testMacPoolCleanupForStatelessVmAfterReturningToStateInSnapshotWithDuplicateMacs() {
        /*
         * we did update when nic was snapshotted while having old_mac. Then it was updated with new_mac, and then we
         * updated it back again so that the nic have again the former mac, which is also stored in snapshot. Now we are
         * about to release mac value, from which we are reverting back to state in snapshot. This mac is not used in
         * snapshot, was related only to stateless vm, thus should be released. Other nic acts here as
         * a duplicity.
         */
        otherNic.setMacAddress(OLD_MAC);
        nicBeingUpdated.setMacAddress(NEW_MAC);
        updatingNic.setMacAddress(OLD_MAC);
        mockOriginalSnapshot(OLD_MAC, OLD_MAC);

        underTest.initMacPoolData();

        //simulate command succeeded.
        underTest.getReturnValue().setSucceeded(true);
        underTest.macPoolCleanupAfterExecution(false);

        verify(macPool).freeMac(NEW_MAC);
    }

    private void setVmAsRunningStateless() {
        vmOwningNicsBeingExtracted.setStateless(true);
        vmOwningNicsBeingExtracted.setStatus(VMStatus.Up);
    }

    private void mockOriginalSnapshot(String... macs) {
        when(snapshotsManager.macsInStatelessSnapshot(VM_ID))
                .thenReturn(Arrays.stream(macs));
    }
}
