package org.ovirt.engine.core.bll.network.vm;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.errors.EngineException;

public class AllocatingMacsInUpdateVmInterfaceCommandTest extends AllocateReleaseMacWhenBeingReservedForSnapshotTest {
    @Test
    public void testAllocateMacFromRequestForNonStatelessVmWhenMacDiffers() {
        nicBeingUpdated.setMacAddress(OLD_MAC);
        updatingNic.setMacAddress(NEW_MAC);

        when(macPool.addMac(updatingNic.getMacAddress())).thenReturn(true);

        underTest.initMacPoolData();
        assertThat("There's no snapshot, new mac of updated nic must be allocated in mac pool",
                underTest.allocateMacFromRequest(), is(true));
    }

    @Test
    public void testAllocateMacFromRequestForNonStatelessVmWhenMacDiffersButItsNotAvailable() {
        nicBeingUpdated.setMacAddress(OLD_MAC);
        updatingNic.setMacAddress(NEW_MAC);

        when(macPool.addMac(updatingNic.getMacAddress())).thenReturn(false);

        assertThrows(EngineException.class, () -> {
                    underTest.initMacPoolData();
                    underTest.allocateMacFromRequest();
                });

        //There's no snapshot, new mac of updated nic must be allocated in mac pool, but mac is not available
    }

    @Test
    public void testAllocateMacFromRequestForNonStatelessVmWhenMacIsUntouched() {
        String sameMac = "sameMac";
        nicBeingUpdated.setMacAddress(sameMac);
        updatingNic.setMacAddress(sameMac);

        underTest.initMacPoolData();
        assertThat("mac is not changed, no action with mac pool should be performed",
                underTest.allocateMacFromRequest(), is(false));
        verifyNoMoreInteractions(macPool);
    }

    @Test
    public void testAllocateMacFromRequestForStatelessVmWhenDivergingFromSnapshot() {
        //we're updating nic with new mac address, old mac address is equal to one stored in snapshot.
        nicBeingUpdated.setMacAddress(OLD_MAC);
        updatingNic.setMacAddress(NEW_MAC);
        mockOriginalSnapshot(OLD_MAC, OTHER_MAC);

        when(macPool.addMac(updatingNic.getMacAddress())).thenReturn(true);

        underTest.initMacPoolData();
        assertThat("New mac value of nic being updated is different as one in snapshot, thus is has to be allocated",
                underTest.allocateMacFromRequest(), is(true));
    }

    @Test
    public void testAllocateMacFromRequestForStatelessVmWhenReturningToStateInSnapshot() {
        /*
         * nic is snapshotted while having old_mac. Then it was updated with new_mac, and now we're updating it again
         * so that the nic will have again the former mac, which is also stored in snapshot.
         */
        nicBeingUpdated.setMacAddress(NEW_MAC);
        updatingNic.setMacAddress(OLD_MAC);
        mockOriginalSnapshot(OLD_MAC, OTHER_MAC);

        underTest.initMacPoolData();
        assertThat("New mac value of nic being updated is same as one in snapshot, thus it is already allocated in"
                        + "mac pool and it must not be allocated again.",
                underTest.allocateMacFromRequest(), is(false));
    }

    @Test
    public void testAllocateMacFromRequestForStatelessVmWhenReturningToStateInSnapshotWithDuplicateMacs() {
        /*
         * nic is snapshotted while having old_mac. Then it was updated with new_mac, and now we're updating it again
         * so that the nic will have again the former mac, which is also stored in snapshot. Other nic acts here as
         * a duplicity.
         */
        otherNic.setMacAddress(OLD_MAC);
        nicBeingUpdated.setMacAddress(NEW_MAC);
        updatingNic.setMacAddress(OLD_MAC);
        mockOriginalSnapshot(OLD_MAC, OLD_MAC);

        underTest.initMacPoolData();
        assertThat("New mac value of nic being updated is same as one in snapshot, thus it is already allocated in"
                        + "mac pool and it must not be allocated again.",
                underTest.allocateMacFromRequest(), is(false));
    }

    private void mockOriginalSnapshot(String... macs) {
        when(snapshotsManager.macsInStatelessSnapshot(VM_ID))
                .thenReturn(Arrays.stream(macs));
    }
}
