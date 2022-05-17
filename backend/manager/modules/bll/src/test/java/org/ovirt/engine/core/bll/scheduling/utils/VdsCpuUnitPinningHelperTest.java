package org.ovirt.engine.core.bll.scheduling.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.businessentities.CpuPinningPolicy;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsCpuUnit;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsNumaNodeDao;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.VdsManager;

@MockitoSettings(strictness = Strictness.LENIENT)
public class VdsCpuUnitPinningHelperTest {

    @Mock
    private VdsManager vdsManager;
    @Mock
    private ResourceManager resourceManager;
    @Mock
    private VdsNumaNodeDao vdsNumaNodeDao;
    @InjectMocks
    @Spy
    private VdsCpuUnitPinningHelper vdsCpuUnitPinningHelper;
    List<VdsCpuUnit> cpuTopology;
    VDS host;

    @BeforeEach
    public void setup() {
        cpuTopology = new ArrayList<>();
        cpuTopology.add(new VdsCpuUnit(0, 0, 0, 0));
        cpuTopology.add(new VdsCpuUnit(0, 0, 0, 1));
        cpuTopology.add(new VdsCpuUnit(0, 0, 1, 2));
        cpuTopology.add(new VdsCpuUnit(0, 0, 1, 3));
        cpuTopology.add(new VdsCpuUnit(1, 1, 0, 4));
        cpuTopology.add(new VdsCpuUnit(1, 1, 0, 5));
        cpuTopology.add(new VdsCpuUnit(1, 1, 1, 6));
        cpuTopology.add(new VdsCpuUnit(1, 1, 1, 7));
        cpuTopology.add(new VdsCpuUnit(2, 2, 0, 8));
        cpuTopology.add(new VdsCpuUnit(2, 2, 0, 9));
        cpuTopology.add(new VdsCpuUnit(2, 2, 1, 10));
        cpuTopology.add(new VdsCpuUnit(2, 2, 1, 11));
        host = new VDS();
        host.setId(Guid.EVERYONE);
        host.setCpuSockets(3);
        host.setCpuThreads(12);
        host.setCpuCores(6);

        when(vdsManager.getCpuTopology()).thenReturn(cpuTopology);
        when(resourceManager.getVdsManager(host.getId())).thenReturn(vdsManager);
        when(resourceManager.getVdsManager(host.getId(), false)).thenReturn(vdsManager);
    }

    @Test
    public void shouldSucceedToAllocateOneSocketOneCoreOneCpu() {
        VM vm = new VM();
        vm.setId(Guid.newGuid());
        vm.setNumOfSockets(1);
        vm.setCpuPerSocket(1);
        vm.setThreadsPerCpu(1);
        vm.setCpuPinningPolicy(CpuPinningPolicy.DEDICATED);
        boolean result =
                vdsCpuUnitPinningHelper.isExclusiveCpuPinningPossibleOnHost(new HashMap<>(), Collections.singletonList(vm), host.getId(), cpuTopology);
        assertTrue(result);
    }

    @Test
    public void shouldSucceedToAllocateOneSocketOneCoreOneCpuWithPendingCpus() {
        VM vm = new VM();
        vm.setId(Guid.newGuid());
        vm.setNumOfSockets(1);
        vm.setCpuPerSocket(1);
        vm.setThreadsPerCpu(1);
        vm.setCpuPinningPolicy(CpuPinningPolicy.DEDICATED);

        List<VdsCpuUnit> pendingCpus = new ArrayList<>();
        pendingCpus.add(new VdsCpuUnit(0, 0, 0, 0));
        pendingCpus.add(new VdsCpuUnit(0, 0, 0, 1));
        Map<Guid, List<VdsCpuUnit>> vmToPendingCpus = new HashMap<>();
        vmToPendingCpus.put(Guid.Empty, pendingCpus);
        boolean result =
                vdsCpuUnitPinningHelper.isExclusiveCpuPinningPossibleOnHost(vmToPendingCpus, Collections.singletonList(vm), host.getId(), cpuTopology);

        assertTrue(result);
    }

    @Test
    public void shouldSucceedToAllocateOneSocketTwoCoreOneCpu() {
        VM vm = new VM();
        vm.setId(Guid.newGuid());
        vm.setNumOfSockets(1);
        vm.setCpuPerSocket(2);
        vm.setThreadsPerCpu(1);
        vm.setCpuPinningPolicy(CpuPinningPolicy.DEDICATED);
        boolean result =
                vdsCpuUnitPinningHelper.isExclusiveCpuPinningPossibleOnHost(new HashMap<>(), Collections.singletonList(vm), host.getId(), cpuTopology);
        assertTrue(result);
    }

    @Test
    public void shouldSucceedToAllocateTwoSocketOneCoreOneCpu() {
        VM vm = new VM();
        vm.setId(Guid.newGuid());
        vm.setNumOfSockets(2);
        vm.setCpuPerSocket(1);
        vm.setThreadsPerCpu(1);
        vm.setCpuPinningPolicy(CpuPinningPolicy.DEDICATED);
        boolean result =
                vdsCpuUnitPinningHelper.isExclusiveCpuPinningPossibleOnHost(new HashMap<>(), Collections.singletonList(vm), host.getId(), cpuTopology);
        assertTrue(result);
    }

    @Test
    public void shouldSucceedToAllocateTwoSocketOneCoreTwoCpu() {
        VM vm = new VM();
        vm.setId(Guid.newGuid());
        vm.setNumOfSockets(2);
        vm.setCpuPerSocket(1);
        vm.setThreadsPerCpu(2);
        vm.setCpuPinningPolicy(CpuPinningPolicy.DEDICATED);
        boolean result =
                vdsCpuUnitPinningHelper.isExclusiveCpuPinningPossibleOnHost(new HashMap<>(), Collections.singletonList(vm), host.getId(), cpuTopology);
        assertTrue(result);
    }

    @Test
    public void shouldFailToAllocateTwoSocketThreeCoreTwoCpu() {
        VM vm = new VM();
        vm.setId(Guid.newGuid());
        vm.setNumOfSockets(2);
        vm.setCpuPerSocket(3);
        vm.setThreadsPerCpu(2);
        vm.setCpuPinningPolicy(CpuPinningPolicy.DEDICATED);
        boolean result =
                vdsCpuUnitPinningHelper.isExclusiveCpuPinningPossibleOnHost(new HashMap<>(), Collections.singletonList(vm), host.getId(), cpuTopology);
        assertFalse(result);
    }

    @Test
    public void shouldSucceedToAllocateThreeSocketTwoCoreTwoCpu() {
        VM vm = new VM();
        vm.setId(Guid.newGuid());
        vm.setNumOfSockets(3);
        vm.setCpuPerSocket(2);
        vm.setThreadsPerCpu(2);
        vm.setCpuPinningPolicy(CpuPinningPolicy.DEDICATED);
        boolean result =
                vdsCpuUnitPinningHelper.isExclusiveCpuPinningPossibleOnHost(new HashMap<>(), Collections.singletonList(vm), host.getId(), cpuTopology);
        assertTrue(result);
    }

    @Test
    public void shouldFailToAllocateTwoSocketFourCoreOneCpu() {
        VM vm = new VM();
        vm.setId(Guid.newGuid());
        vm.setNumOfSockets(2);
        vm.setCpuPerSocket(4);
        vm.setThreadsPerCpu(2);
        vm.setCpuPinningPolicy(CpuPinningPolicy.DEDICATED);
        boolean result =
                vdsCpuUnitPinningHelper.isExclusiveCpuPinningPossibleOnHost(new HashMap<>(), Collections.singletonList(vm), host.getId(), cpuTopology);
        assertFalse(result);
    }


    @Test
    public void shouldSucceedToDedicateOneSocketOneCoreOneCpu() {
        VM vm = new VM();
        vm.setId(Guid.newGuid());
        vm.setNumOfSockets(1);
        vm.setCpuPerSocket(1);
        vm.setThreadsPerCpu(1);
        vm.setCpuPinningPolicy(CpuPinningPolicy.DEDICATED);
        List<VdsCpuUnit> cpus = vdsCpuUnitPinningHelper.updatePhysicalCpuAllocations(vm, new HashMap<>(), host.getId());
        assertEquals(1, cpus.size());
        assertEquals(0, cpus.get(0).getSocket());
        assertEquals(0, cpus.get(0).getCore());
        assertEquals(0, cpus.get(0).getCpu());
    }

    @Test
    public void shouldSucceedToDedicateOneSocketTwoCoreOneCpu() {
        VM vm = new VM();
        vm.setId(Guid.newGuid());
        vm.setNumOfSockets(1);
        vm.setCpuPerSocket(2);
        vm.setThreadsPerCpu(1);
        vm.setCpuPinningPolicy(CpuPinningPolicy.DEDICATED);
        List<VdsCpuUnit> cpus = vdsCpuUnitPinningHelper.updatePhysicalCpuAllocations(vm, new HashMap<>(), host.getId());
        assertEquals(2, cpus.size());
        assertEquals(0, cpus.get(0).getSocket());
        assertEquals(0, cpus.get(0).getCore());
        assertEquals(0, cpus.get(0).getCpu());

        assertEquals(0, cpus.get(1).getSocket());
        assertEquals(0, cpus.get(1).getCore());
        assertEquals(1, cpus.get(1).getCpu());
    }

    @Test
    public void shouldSucceedToDedicateTwoSocketOneCoreOneCpu() {
        VM vm = new VM();
        vm.setId(Guid.newGuid());
        vm.setNumOfSockets(2);
        vm.setCpuPerSocket(1);
        vm.setThreadsPerCpu(1);
        vm.setCpuPinningPolicy(CpuPinningPolicy.DEDICATED);
        List<VdsCpuUnit> cpus = vdsCpuUnitPinningHelper.updatePhysicalCpuAllocations(vm, new HashMap<>(), host.getId());
        assertEquals(2, cpus.size());
        assertEquals(0, cpus.get(0).getSocket());
        assertEquals(0, cpus.get(0).getCore());
        assertEquals(0, cpus.get(0).getCpu());

        assertEquals(0, cpus.get(1).getSocket());
        assertEquals(0, cpus.get(1).getCore());
        assertEquals(1, cpus.get(1).getCpu());
    }

    @Test
    public void shouldSucceedToDedicateTwoSocketOneCoreTwoCpu() {
        VM vm = new VM();
        vm.setId(Guid.newGuid());
        vm.setNumOfSockets(2);
        vm.setCpuPerSocket(1);
        vm.setThreadsPerCpu(2);
        vm.setCpuPinningPolicy(CpuPinningPolicy.DEDICATED);
        List<VdsCpuUnit> cpus = vdsCpuUnitPinningHelper.updatePhysicalCpuAllocations(vm, new HashMap<>(), host.getId());
        assertEquals(4, cpus.size());

        assertEquals(0, cpus.get(0).getSocket());
        assertEquals(0, cpus.get(0).getCore());
        assertEquals(0, cpus.get(0).getCpu());

        assertEquals(0, cpus.get(1).getSocket());
        assertEquals(0, cpus.get(1).getCore());
        assertEquals(1, cpus.get(1).getCpu());

        assertEquals(0, cpus.get(2).getSocket());
        assertEquals(1, cpus.get(2).getCore());
        assertEquals(2, cpus.get(2).getCpu());

        assertEquals(0, cpus.get(3).getSocket());
        assertEquals(1, cpus.get(3).getCore());
        assertEquals(3, cpus.get(3).getCpu());
    }

    @Test
    public void shouldFailToDedicateThreeSocketFiveCoreOneCpu() {
        VM vm = new VM();
        vm.setId(Guid.newGuid());
        vm.setNumOfSockets(3);
        vm.setCpuPerSocket(5);
        vm.setThreadsPerCpu(1);
        vm.setCpuPinningPolicy(CpuPinningPolicy.DEDICATED);
        List<VdsCpuUnit> cpus = vdsCpuUnitPinningHelper.updatePhysicalCpuAllocations(vm, new HashMap<>(), host.getId());
        assertNull(cpus);
    }

    @Test
    public void shouldSucceedToDedicateOneSocketThreeCoreOneCpu() {
        VM vm = new VM();
        vm.setId(Guid.newGuid());
        vm.setNumOfSockets(1);
        vm.setCpuPerSocket(3);
        vm.setThreadsPerCpu(1);
        vm.setCpuPinningPolicy(CpuPinningPolicy.DEDICATED);
        List<VdsCpuUnit> cpus = vdsCpuUnitPinningHelper.updatePhysicalCpuAllocations(vm, new HashMap<>(), host.getId());
        assertEquals(3, cpus.size());

        assertEquals(0, cpus.get(0).getSocket());
        assertEquals(0, cpus.get(0).getCore());
        assertEquals(0, cpus.get(0).getCpu());

        assertEquals(0, cpus.get(1).getSocket());
        assertEquals(0, cpus.get(1).getCore());
        assertEquals(1, cpus.get(1).getCpu());

        assertEquals(0, cpus.get(2).getSocket());
        assertEquals(1, cpus.get(2).getCore());
        assertEquals(2, cpus.get(2).getCpu());
    }

    @Test
    public void shouldSucceedToDedicateSplitSockets() {
        VM vm = new VM();
        vm.setId(Guid.newGuid());
        vm.setNumOfSockets(2);
        vm.setCpuPerSocket(1);
        vm.setThreadsPerCpu(1);
        VDS host = new VDS();
        host.setId(Guid.EVERYONE);
        host.setCpuSockets(2);
        host.setCpuThreads(4);
        host.setCpuCores(2);
        vm.setCpuPinningPolicy(CpuPinningPolicy.DEDICATED);
        List<VdsCpuUnit> cpuTopology = new ArrayList<>();
        cpuTopology.add(new VdsCpuUnit(0, 0, 0, 0));
        cpuTopology.add(new VdsCpuUnit(1, 1, 0, 0));
        when(vdsManager.getCpuTopology()).thenReturn(cpuTopology);

        List<VdsCpuUnit> cpus = vdsCpuUnitPinningHelper.updatePhysicalCpuAllocations(vm, new HashMap<>(), host.getId());
        assertEquals(2, cpus.size());

        assertEquals(0, cpus.get(0).getSocket());
        assertEquals(0, cpus.get(0).getCore());
        assertEquals(0, cpus.get(0).getCpu());

        assertEquals(1, cpus.get(1).getSocket());
        assertEquals(0, cpus.get(1).getCore());
        assertEquals(0, cpus.get(1).getCpu());
    }

    @Test
    public void shouldSucceedToDedicateSplitSocketsWithTakenCPUs() {
        VM vm = new VM();
        vm.setId(Guid.newGuid());
        vm.setNumOfSockets(2);
        vm.setCpuPerSocket(1);
        vm.setThreadsPerCpu(1);
        VDS host = new VDS();
        host.setId(Guid.EVERYONE);
        host.setCpuSockets(2);
        host.setCpuThreads(4);
        host.setCpuCores(2);
        vm.setCpuPinningPolicy(CpuPinningPolicy.DEDICATED);
        List<VdsCpuUnit> cpuTopology = new ArrayList<>();
        cpuTopology.add(new VdsCpuUnit(0, 0, 0, 0));
        VdsCpuUnit vdsCpuUnit = new VdsCpuUnit(0, 0, 0, 1);
        vdsCpuUnit.pinVm(Guid.newGuid(), CpuPinningPolicy.DEDICATED);
        cpuTopology.add(vdsCpuUnit);
        cpuTopology.add(new VdsCpuUnit(1, 1, 0, 0));
        vdsCpuUnit = new VdsCpuUnit(1, 1, 0, 1);
        vdsCpuUnit.pinVm(Guid.newGuid(), CpuPinningPolicy.DEDICATED);
        cpuTopology.add(vdsCpuUnit);
        when(vdsManager.getCpuTopology()).thenReturn(cpuTopology);

        List<VdsCpuUnit> cpus = vdsCpuUnitPinningHelper.updatePhysicalCpuAllocations(vm, new HashMap<>(), host.getId());
        assertEquals(2, cpus.size());

        assertEquals(0, cpus.get(0).getSocket());
        assertEquals(0, cpus.get(0).getCore());
        assertEquals(0, cpus.get(0).getCpu());

        assertEquals(1, cpus.get(1).getSocket());
        assertEquals(0, cpus.get(1).getCore());
        assertEquals(0, cpus.get(1).getCpu());
    }

    @Test
    public void shouldFailToDedicateOneSocketThreeCoreOneCpuOffLine() {
        VM vm = new VM();
        vm.setId(Guid.newGuid());
        vm.setNumOfSockets(1);
        vm.setCpuPerSocket(3);
        vm.setThreadsPerCpu(1);
        vm.setCpuPinningPolicy(CpuPinningPolicy.DEDICATED);
        List<VdsCpuUnit> cpuTopology = new ArrayList<>();
        cpuTopology.add(new VdsCpuUnit(0, 0, 0, 0));
        cpuTopology.add(new VdsCpuUnit(1, 1, 0, 1));
        cpuTopology.add(new VdsCpuUnit(2, 2, 1, 0));
        when(vdsManager.getCpuTopology()).thenReturn(cpuTopology);
        List<VdsCpuUnit> cpus = vdsCpuUnitPinningHelper.updatePhysicalCpuAllocations(vm, new HashMap<>(), host.getId());
        assertNull(cpus);
    }

    @Test
    public void shouldSucceedToDedicateEqualSockets() {
        VM vm = new VM();
        vm.setId(Guid.newGuid());
        vm.setNumOfSockets(2);
        vm.setCpuPerSocket(3);
        vm.setThreadsPerCpu(2);
        vm.setCpuPinningPolicy(CpuPinningPolicy.DEDICATED);
        List<VdsCpuUnit> cpuTopology = new ArrayList<>();
        cpuTopology.add(new VdsCpuUnit(0, 0, 0, 0));
        cpuTopology.add(new VdsCpuUnit(0, 0, 0, 1));
        cpuTopology.add(new VdsCpuUnit(0, 0, 1, 0));
        VdsCpuUnit vdsmTaken = new VdsCpuUnit(0, 0, 1, 1);
        vdsmTaken.pinVm(Guid.SYSTEM, CpuPinningPolicy.MANUAL);
        cpuTopology.add(vdsmTaken);
        cpuTopology.add(new VdsCpuUnit(0, 0, 2, 0));
        cpuTopology.add(new VdsCpuUnit(0, 0, 2, 1));
        cpuTopology.add(new VdsCpuUnit(0, 0, 3, 0));
        cpuTopology.add(new VdsCpuUnit(0, 0, 3, 1));
        cpuTopology.add(new VdsCpuUnit(1, 1, 0, 0));
        cpuTopology.add(new VdsCpuUnit(1, 1, 0, 1));
        cpuTopology.add(new VdsCpuUnit(1, 1, 1, 0));
        cpuTopology.add(new VdsCpuUnit(1, 1, 1, 1));
        cpuTopology.add(new VdsCpuUnit(1, 1, 2, 0));
        cpuTopology.add(new VdsCpuUnit(1, 1, 2, 1));
        cpuTopology.add(new VdsCpuUnit(1, 1, 3, 0));
        cpuTopology.add(new VdsCpuUnit(1, 1, 3, 1));
        when(vdsManager.getCpuTopology()).thenReturn(cpuTopology);
        List<VdsCpuUnit> cpus = vdsCpuUnitPinningHelper.updatePhysicalCpuAllocations(vm, new HashMap<>(), host.getId());
        assertEquals(12, cpus.size());

        assertEquals(6, cpus.stream().filter(cpu -> cpu.getSocket() == 0).count());
        assertEquals(6, cpus.stream().filter(cpu -> cpu.getSocket() == 1).count());
    }

}
