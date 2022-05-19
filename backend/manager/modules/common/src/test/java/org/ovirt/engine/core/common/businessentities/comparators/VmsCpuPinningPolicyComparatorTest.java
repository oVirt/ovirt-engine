package org.ovirt.engine.core.common.businessentities.comparators;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.CpuPinningPolicy;
import org.ovirt.engine.core.common.businessentities.VM;

public class VmsCpuPinningPolicyComparatorTest {

    private VM vm1;
    private VM vm2;
    VmsCpuPinningPolicyComparator comparator = new VmsCpuPinningPolicyComparator();

    private void verifyResult(VM vm1, VM vm2, int expectedResult) {
        assertEquals(expectedResult, comparator.compare(vm1, vm2),
                String.format("Expected %1$s to be %3$s %2$s, but it wasn't.",
                        vm1.getName(),
                        vm2.getName(),
                        expectedResult == -1 ? "less than" : expectedResult == 1 ? "greater than" : "equal to"));
    }

    @BeforeEach
    public void setUp() {
        vm1 = new VM();
        vm1.setName("vm1");
        vm2 = new VM();
        vm2.setName("vm2");
    }

    @Test
    public void testEqualNone() {
        verifyResult(vm1, vm2, 0);
    }

    @Test
    public void testManual() {
        vm1.setCpuPinningPolicy(CpuPinningPolicy.MANUAL);
        // vm2 is none
        verifyResult(vm1, vm2, 1);
        vm2.setCpuPinningPolicy(CpuPinningPolicy.RESIZE_AND_PIN_NUMA);
        verifyResult(vm1, vm2, 1);
        vm2.setCpuPinningPolicy(CpuPinningPolicy.ISOLATE_THREADS);
        verifyResult(vm1, vm2, 1);
        vm2.setCpuPinningPolicy(CpuPinningPolicy.DEDICATED);
        verifyResult(vm1, vm2, 1);
    }

    @Test
    public void testResize() {
        vm1.setCpuPinningPolicy(CpuPinningPolicy.RESIZE_AND_PIN_NUMA);
        // vm2 is none
        verifyResult(vm1, vm2, 1);
        vm2.setCpuPinningPolicy(CpuPinningPolicy.MANUAL);
        verifyResult(vm1, vm2, -1);
        vm2.setCpuPinningPolicy(CpuPinningPolicy.RESIZE_AND_PIN_NUMA);
        verifyResult(vm1, vm2, 0);
        vm2.setCpuPinningPolicy(CpuPinningPolicy.ISOLATE_THREADS);
        verifyResult(vm1, vm2, 1);
        vm2.setCpuPinningPolicy(CpuPinningPolicy.DEDICATED);
        verifyResult(vm1, vm2, 1);
    }

    @Test
    public void testIsolateThreads() {
        vm1.setCpuPinningPolicy(CpuPinningPolicy.ISOLATE_THREADS);
        // vm2 is none
        verifyResult(vm1, vm2, 1);
        vm2.setCpuPinningPolicy(CpuPinningPolicy.MANUAL);
        verifyResult(vm1, vm2, -1);
        vm2.setCpuPinningPolicy(CpuPinningPolicy.RESIZE_AND_PIN_NUMA);
        verifyResult(vm1, vm2, -1);
        vm2.setCpuPinningPolicy(CpuPinningPolicy.ISOLATE_THREADS);
        verifyResult(vm1, vm2, 0);
        vm2.setCpuPinningPolicy(CpuPinningPolicy.DEDICATED);
        verifyResult(vm1, vm2, 1);
    }

    @Test
    public void testDedicated() {
        vm1.setCpuPinningPolicy(CpuPinningPolicy.DEDICATED);
        // vm2 is none
        verifyResult(vm1, vm2, 1);
        vm2.setCpuPinningPolicy(CpuPinningPolicy.MANUAL);
        verifyResult(vm1, vm2, -1);
        vm2.setCpuPinningPolicy(CpuPinningPolicy.RESIZE_AND_PIN_NUMA);
        verifyResult(vm1, vm2, -1);
        vm2.setCpuPinningPolicy(CpuPinningPolicy.ISOLATE_THREADS);
        verifyResult(vm1, vm2, -1);
        vm2.setCpuPinningPolicy(CpuPinningPolicy.DEDICATED);
        verifyResult(vm1, vm2, 0);
    }

    @Test
    public void testNumOfCpus() {
        vm1.setCpuPinningPolicy(CpuPinningPolicy.MANUAL);
        vm2.setCpuPinningPolicy(CpuPinningPolicy.MANUAL);
        vm2.setNumOfSockets(2);
        verifyResult(vm1, vm2, -1);
    }

    @Test
    public void testCpuTopologyThreads() {
        vm1.setThreadsPerCpu(2);
        vm2.setCpuPerSocket(2);
        verifyResult(vm1, vm2, 1);
    }

    @Test
    public void testCpuTopologyCores() {
        vm1.setCpuPerSocket(2);
        vm2.setNumOfSockets(2);
        verifyResult(vm1, vm2, 1);
    }

    @Test
    public void testCpuTopologySockets() {
        vm1.setNumOfSockets(3);
        vm2.setNumOfSockets(2);
        verifyResult(vm1, vm2, 1);
    }

    @Test
    public void vmSort() {
        ArrayList<VM> vms = new ArrayList<>();
        vms.add(vm1);
        vms.add(vm2);
        vm2.setCpuPinningPolicy(CpuPinningPolicy.MANUAL);
        vms.sort(comparator.reversed());
        assertEquals(vms.get(0), vm2);
        assertEquals(vms.get(1), vm1);
    }

}
