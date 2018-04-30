package org.ovirt.engine.core.bll.scheduling;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.utils.VmOverheadCalculatorImpl;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.OsType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.scheduling.VmOverheadCalculator;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SlaValidatorTest {

    @Spy
    VmOverheadCalculator vmOverheadCalculator = new VmOverheadCalculatorImpl();

    @InjectMocks
    SlaValidator slaValidator = new SlaValidator();

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

    @BeforeEach
    public void prepareTest() {
        vds = basicHost();
        vm = basicVm();

        doReturn(65).when(vmOverheadCalculator).getStaticOverheadInMb(any());
        doReturn(0).when(vmOverheadCalculator).getPossibleOverheadInMb(any());
        when(vmOverheadCalculator.getTotalRequiredMemoryInMb(vm)).thenCallRealMethod();
        when(vmOverheadCalculator.getOverheadInMb(vm)).thenCallRealMethod();
    }

    // VM start tests - the host has to have enough physical memory to call malloc

    @Test
    public void validateVmMemoryCanStartOnVds() {
        boolean vmPassedMemoryRequirement = slaValidator.hasPhysMemoryToRunVM(vds, vm, 0);
        assertTrue(vmPassedMemoryRequirement);
    }

    @Test
    public void validateVmMemoryCantStartOnVdsBecauseOfPending() {
        boolean vmPassedMemoryRequirement = slaValidator.hasPhysMemoryToRunVM(vds, vm, 65);
        assertFalse(vmPassedMemoryRequirement);
    }


    @Test
    public void validateVmMemoryCanStartOnVdsBecauseOfLowGuestOverhead() {
        doReturn(0).when(vmOverheadCalculator).getStaticOverheadInMb(any());
        boolean vmPassedMemoryRequirement = slaValidator.hasPhysMemoryToRunVM(vds, vm, 65);
        assertTrue(vmPassedMemoryRequirement);
    }

    @Test
    public void validateVmMemoryCantStartOnVdsBecauseOfGuestOverhead() {
        doReturn(256).when(vmOverheadCalculator).getStaticOverheadInMb(any());
        boolean vmPassedMemoryRequirement = slaValidator.hasPhysMemoryToRunVM(vds, vm, 0);
        assertFalse(vmPassedMemoryRequirement);
    }

    @Test
    public void validateVmMemoryCantStartOnVdsVmTooBig() {
        vm.setMinAllocatedMem(8865);
        vm.setVmMemSizeMb(8865);
        boolean vmPassedMemoryRequirement = slaValidator.hasPhysMemoryToRunVM(vds, vm, 0);
        assertFalse(vmPassedMemoryRequirement);
    }

    @Test
    public void validateVmMemoryCanStartOnVdsVmTooBigButLowOverhead() {
        vm.setMinAllocatedMem(8865);
        vm.setVmMemSizeMb(8865);
        doReturn(0).when(vmOverheadCalculator).getStaticOverheadInMb(any());
        boolean vmPassedMemoryRequirement = slaValidator.hasPhysMemoryToRunVM(vds, vm, 0);
        assertTrue(vmPassedMemoryRequirement);
    }

    @Test
    public void validateVmMemoryCantStartOnVdsHostTooSmall() {
        vds.setMemFree(8835L);
        boolean vmPassedMemoryRequirement = slaValidator.hasPhysMemoryToRunVM(vds, vm, 0);
        assertFalse(vmPassedMemoryRequirement);
    }

    @Test
    public void validateVmMemoryCanStartOnVdsHostTooSmallButLowOverhead() {
        vds.setMemFree(8835L);
        doReturn(0).when(vmOverheadCalculator).getStaticOverheadInMb(any());
        boolean vmPassedMemoryRequirement = slaValidator.hasPhysMemoryToRunVM(vds, vm, 0);
        assertTrue(vmPassedMemoryRequirement);
    }

    @Test
    public void validateVmMemoryCanStartOnVdsHighPendingButSwapHelps() {
        vds.setSwapFree(80L);
        boolean vmPassedMemoryRequirement = slaValidator.hasPhysMemoryToRunVM(vds, vm, 65);
        assertTrue(vmPassedMemoryRequirement);
    }

    @Test
    public void validateVmMemoryCantStartOnVdsHighPendingSwapSmall() {
        vds.setSwapFree(60L);
        boolean vmPassedMemoryRequirement = slaValidator.hasPhysMemoryToRunVM(vds, vm, 65);
        assertTrue(vmPassedMemoryRequirement);
    }

    // Test overcommit rules for VM to host assignments


    @Test
    public void validateVmMemoryCanRunOnVds() {
        boolean vmPassedMemoryRequirement = slaValidator.hasOvercommitMemoryToRunVM(vds, vm);
        assertTrue(vmPassedMemoryRequirement);
    }

    @Test
    public void validateVmMemoryCantRunOnVdsNotEnoughMem() {
        vm.setMinAllocatedMem(10000);
        vm.setVmMemSizeMb(10000);
        boolean vmPassedMemoryRequirement = slaValidator.hasOvercommitMemoryToRunVM(vds, vm);
        assertFalse(vmPassedMemoryRequirement);
    }

    @Test
    public void validateVmMemoryCanRunOnVdsHighMem() {
        vm.setMinAllocatedMem(10000);
        vm.setVmMemSizeMb(10000);
        vds.setPhysicalMemMb(15000);
        boolean vmPassedMemoryRequirement = slaValidator.hasOvercommitMemoryToRunVM(vds, vm);
        assertTrue(vmPassedMemoryRequirement);
    }

    @Test
    public void validateVmMemoryCantRunOnVdsSmallHost() {
        vds.setPhysicalMemMb(5000);
        boolean vmPassedMemoryRequirement = slaValidator.hasOvercommitMemoryToRunVM(vds, vm);
        assertFalse(vmPassedMemoryRequirement);
    }

    @Test
    public void validateVmMemoryCantRunOnVdsHighOverhead() {
        doReturn(1024).when(vmOverheadCalculator).getStaticOverheadInMb(any());
        boolean vmPassedMemoryRequirement = slaValidator.hasOvercommitMemoryToRunVM(vds, vm);
        assertFalse(vmPassedMemoryRequirement);
    }

    @Test
    public void validateVmMemoryCanRunOnVdsHighMemLowOverhead() {
        vm.setMinAllocatedMem(10000);
        vm.setVmMemSizeMb(10000);
        doReturn(-1200).when(vmOverheadCalculator).getStaticOverheadInMb(any());
        boolean vmPassedMemoryRequirement = slaValidator.hasOvercommitMemoryToRunVM(vds, vm);
        assertTrue(vmPassedMemoryRequirement);
    }

    private VM basicVm() {
        VM vm = new VM();
        vm.setId(Guid.newGuid());
        vm.setMinAllocatedMem(8800);
        vm.setVmMemSizeMb(8800);
        vm.setClusterArch(ArchitectureType.x86_64);
        vm.setGuestOsType(OsType.Linux);
        vm.setVmOs(24);
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
