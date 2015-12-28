package org.ovirt.engine.core.bll.scheduling;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;

public class SlaValidatorTest {

    private VDS makeTestVds(Guid vdsId) {
        VDS newVdsData = new VDS();
        newVdsData.setHostName("BUZZ");
        newVdsData.setVdsName("BAR");
        newVdsData.setClusterCompatibilityVersion(new Version("1.2.3"));
        newVdsData.setClusterId(Guid.newGuid());
        newVdsData.setId(vdsId);
        return newVdsData;
    }

    VDS vds;
    VM vm;

    @Before
    public void prepateTest() {
        vds = basicHost();
        vm = basicVm();
    }

    // VM start tests - the host has to have enough physical memory to call malloc

    @Test
    public void validateVmMemoryCanStartOnVds() {
        boolean vmPassedMemoryRequirement = SlaValidator.getInstance().hasPhysMemoryToRunVM(vds, vm, 0);
        assertEquals(vmPassedMemoryRequirement, true);
    }

    @Test
    public void validateVmMemoryCantStartOnVdsBecauseOfPending() {
        boolean vmPassedMemoryRequirement = SlaValidator.getInstance().hasPhysMemoryToRunVM(vds, vm, 65);
        assertEquals(vmPassedMemoryRequirement, false);
    }


    @Test
    public void validateVmMemoryCanStartOnVdsBecauseOfLowGuestOverhead() {
        vds.setGuestOverhead(0);
        boolean vmPassedMemoryRequirement = SlaValidator.getInstance().hasPhysMemoryToRunVM(vds, vm, 65);
        assertEquals(vmPassedMemoryRequirement, true);
    }

    @Test
    public void validateVmMemoryCantStartOnVdsBecauseOfGuestOverhead() {
        vds.setGuestOverhead(256);
        boolean vmPassedMemoryRequirement = SlaValidator.getInstance().hasPhysMemoryToRunVM(vds, vm, 0);
        assertEquals(vmPassedMemoryRequirement, false);
    }

    @Test
    public void validateVmMemoryCantStartOnVdsVmTooBig() {
        vm.setMinAllocatedMem(8865);
        vm.setVmMemSizeMb(8865);
        boolean vmPassedMemoryRequirement = SlaValidator.getInstance().hasPhysMemoryToRunVM(vds, vm, 0);
        assertEquals(vmPassedMemoryRequirement, false);
    }

    @Test
    public void validateVmMemoryCanStartOnVdsVmTooBigButLowOverhead() {
        vm.setMinAllocatedMem(8865);
        vm.setVmMemSizeMb(8865);
        vds.setGuestOverhead(0);
        boolean vmPassedMemoryRequirement = SlaValidator.getInstance().hasPhysMemoryToRunVM(vds, vm, 0);
        assertEquals(vmPassedMemoryRequirement, true);
    }

    @Test
    public void validateVmMemoryCantStartOnVdsHostTooSmall() {
        vds.setMemFree(8835L);
        boolean vmPassedMemoryRequirement = SlaValidator.getInstance().hasPhysMemoryToRunVM(vds, vm, 0);
        assertEquals(vmPassedMemoryRequirement, false);
    }

    @Test
    public void validateVmMemoryCanStartOnVdsHostTooSmallButLowOverhead() {
        vds.setMemFree(8835L);
        vds.setGuestOverhead(0);
        boolean vmPassedMemoryRequirement = SlaValidator.getInstance().hasPhysMemoryToRunVM(vds, vm, 0);
        assertEquals(vmPassedMemoryRequirement, true);
    }

    @Test
    public void validateVmMemoryCanStartOnVdsHighPendingButSwapHelps() {
        vds.setSwapFree(80L);
        boolean vmPassedMemoryRequirement = SlaValidator.getInstance().hasPhysMemoryToRunVM(vds, vm, 65);
        assertEquals(vmPassedMemoryRequirement, true);
    }

    @Test
    public void validateVmMemoryCantStartOnVdsHighPendingSwapSmall() {
        vds.setSwapFree(60L);
        boolean vmPassedMemoryRequirement = SlaValidator.getInstance().hasPhysMemoryToRunVM(vds, vm, 65);
        assertEquals(vmPassedMemoryRequirement, true);
    }

    // Test overcommit rules for VM to host assignments


    @Test
    public void validateVmMemoryCanRunOnVds() {
        boolean vmPassedMemoryRequirement = SlaValidator.getInstance().hasOvercommitMemoryToRunVM(vds, vm);
        assertEquals(vmPassedMemoryRequirement, true);
    }

    @Test
    public void validateVmMemoryCantRunOnVdsNotEnoughMem() {
        vm.setMinAllocatedMem(10000);
        vm.setVmMemSizeMb(10000);
        boolean vmPassedMemoryRequirement = SlaValidator.getInstance().hasOvercommitMemoryToRunVM(vds, vm);
        assertEquals(vmPassedMemoryRequirement, false);
    }

    @Test
    public void validateVmMemoryCanRunOnVdsHighMem() {
        vm.setMinAllocatedMem(10000);
        vm.setVmMemSizeMb(10000);
        vds.setPhysicalMemMb(15000);
        boolean vmPassedMemoryRequirement = SlaValidator.getInstance().hasOvercommitMemoryToRunVM(vds, vm);
        assertEquals(vmPassedMemoryRequirement, true);
    }

    @Test
    public void validateVmMemoryCantRunOnVdsSmallHost() {
        vds.setPhysicalMemMb(5000);
        boolean vmPassedMemoryRequirement = SlaValidator.getInstance().hasOvercommitMemoryToRunVM(vds, vm);
        assertEquals(vmPassedMemoryRequirement, false);
    }

    @Test
    public void validateVmMemoryCantRunOnVdsHighOverhead() {
        vds.setGuestOverhead(1024);
        boolean vmPassedMemoryRequirement = SlaValidator.getInstance().hasOvercommitMemoryToRunVM(vds, vm);
        assertEquals(vmPassedMemoryRequirement, false);
    }

    @Test
    public void validateVmMemoryCanRunOnVdsHighMemLowOverhead() {
        vm.setMinAllocatedMem(10000);
        vm.setVmMemSizeMb(10000);
        vds.setGuestOverhead(-1200);
        boolean vmPassedMemoryRequirement = SlaValidator.getInstance().hasOvercommitMemoryToRunVM(vds, vm);
        assertEquals(vmPassedMemoryRequirement, true);
    }

    private VM basicVm() {
        VM vm = new VM();
        vm.setId(Guid.newGuid());
        vm.setMinAllocatedMem(8800);
        vm.setVmMemSizeMb(8800);
        return vm;
    }

    private VDS basicHost() {
        Guid guid = Guid.newGuid();
        VDS vds = makeTestVds(guid);
        // HW info and configuration
        vds.setPhysicalMemMb(10000);
        vds.setReservedMem(256);
        vds.setGuestOverhead(65);
        vds.setVmCount(1);

        // One other VM is already running on the host
        vds.setMemCommited(10000);
        vds.setVmCount(2);
        vds.setPendingVmemSize(0);

        // dynamic memory info related to host status and VM starts
        vds.setMemFree(8900L);

        // 200% mem overcommit meaning total of 20GB of maximum VM memory can be assigned to vds
        vds.setMaxVdsMemoryOverCommit(200);
        return vds;
    }

}
