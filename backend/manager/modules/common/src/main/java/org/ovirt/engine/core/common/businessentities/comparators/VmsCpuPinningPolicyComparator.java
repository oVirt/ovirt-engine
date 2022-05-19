package org.ovirt.engine.core.common.businessentities.comparators;

import java.io.Serializable;
import java.util.Comparator;

import org.ovirt.engine.core.common.businessentities.CpuPinningPolicy;
import org.ovirt.engine.core.common.businessentities.VM;

/**
 * Comparing VMs based on the CPU pinning policy and if needed, their CPU topology.
 */
public class VmsCpuPinningPolicyComparator implements Comparator<VM>, Serializable {

    @Override
    public int compare(VM vm1, VM vm2) {
        int diff = CpuPinningPolicy.compare(vm1.getCpuPinningPolicy(), vm2.getCpuPinningPolicy());
        if (diff != 0) {
            return diff;
        }
        diff = vm1.getNumOfCpus() - vm2.getNumOfCpus();
        if (diff != 0) {
            return diff;
        }
        // The policies are equal, numOfCpus is equal, compare based on CPU topology - bottom up.
        diff = vm1.getThreadsPerCpu() - vm2.getThreadsPerCpu();
        if (diff != 0) {
            return diff;
        }
        diff = vm1.getCpuPerSocket() - vm2.getCpuPerSocket();
        if (diff != 0) {
            return diff;
        }
        return vm1.getNumOfSockets() - vm2.getNumOfSockets();
    }
}
