package org.ovirt.engine.core.bll.network.vm;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.network.macpool.MacPool;
import org.ovirt.engine.core.bll.snapshots.SnapshotsManager;
import org.ovirt.engine.core.common.action.AddVmInterfaceParameters;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;
import org.ovirt.engine.core.dao.network.VmNicDao;

@RunWith(MockitoJUnitRunner.class)
public class UpdateVmInterfaceCommandTest {

    private static final CommandContext CMD_CONTEXT = new CommandContext(new EngineContext());
    private static final Guid VM_ID = Guid.newGuid();
    private static final Guid CLUSTER_ID = Guid.newGuid();
    private static final Guid UPDATED_NIC_ID = Guid.newGuid();
    private static final Guid OTHER_NIC_ID = Guid.newGuid();
    private static final String OLD_MAC = "oldMac";
    private static final String NEW_MAC = "newMac";
    private static final String OTHER_MAC = "otherMac";

    private VmNetworkInterface nicBeingUpdated = createVnNetworkInterface("nicBeingUpdated", UPDATED_NIC_ID);
    private VmNetworkInterface updatingNic = createVnNetworkInterface("updatingNic", UPDATED_NIC_ID);
    private VmNetworkInterface otherNic = createVnNetworkInterface("otherNic", OTHER_NIC_ID);

    private AddVmInterfaceParameters parameters = new AddVmInterfaceParameters(VM_ID, updatingNic);

    @InjectMocks
    UpdateVmInterfaceCommand underTest = new UpdateVmInterfaceCommand<>(parameters, CMD_CONTEXT);

    @Mock
    private VmDeviceDao vmDeviceDao;

    @Mock
    private VmNetworkInterfaceDao vmNetworkInterfaceDao;

    @Mock
    private VmNicDao vmNicDao;

    @Mock
    private VmDao vmDao;

    @Mock
    private SnapshotsManager snapshotsManager;

    @Mock
    private MacPool macPool;
    private VM vmOwningNicsBeingExtracted;

    @Before
    public void setUp() {
        underTest.setClusterId(CLUSTER_ID);

        //by default other nic has other mac, which does not interfere with nics being updated.
        otherNic.setMacAddress("otherMac");

        vmOwningNicsBeingExtracted = new VM();
        vmOwningNicsBeingExtracted.setInterfaces(Arrays.asList(nicBeingUpdated, otherNic));


        when(vmNicDao.getAllForVm(VM_ID)).thenReturn(Arrays.asList(nicBeingUpdated, otherNic));
        when(vmDao.get(VM_ID)).thenReturn(vmOwningNicsBeingExtracted);
        when(vmNetworkInterfaceDao.getAllForVm(VM_ID)).thenReturn(vmOwningNicsBeingExtracted.getInterfaces());

        underTest.initVmData();
    }

    @Test
    public void testAllocateMacFromRequestForNonStatelessVmWhenMacDiffers() {
        nicBeingUpdated.setMacAddress(OLD_MAC);
        updatingNic.setMacAddress(NEW_MAC);

        when(macPool.addMac(updatingNic.getMacAddress())).thenReturn(true);

        underTest.initMacPoolData();
        assertThat("There's no snapshot, new mac of updated nic must be allocated in mac pool",
                underTest.allocateMacFromRequest(), is(true));
    }

    @Test(expected = EngineException.class)
    public void testAllocateMacFromRequestForNonStatelessVmWhenMacDiffersButItsNotAvailable() {
        nicBeingUpdated.setMacAddress(OLD_MAC);
        updatingNic.setMacAddress(NEW_MAC);

        when(macPool.addMac(updatingNic.getMacAddress())).thenReturn(false);

        underTest.initMacPoolData();
        underTest.allocateMacFromRequest();

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

    private void mockOriginalSnapshot(String... macs) {
        when(snapshotsManager.macsInStatelessSnapshot(VM_ID))
                .thenReturn(Arrays.stream(macs));
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

    private VmNetworkInterface createVnNetworkInterface(String name, Guid id) {
        VmNetworkInterface vmNetworkInterface = new VmNetworkInterface();

        vmNetworkInterface.setName(name);
        vmNetworkInterface.setId(id);

        return vmNetworkInterface;
    }
}
