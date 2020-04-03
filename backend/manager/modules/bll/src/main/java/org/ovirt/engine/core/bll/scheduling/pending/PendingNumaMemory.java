package org.ovirt.engine.core.bll.scheduling.pending;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.ovirt.engine.core.bll.scheduling.NumaNodeMemoryConsumption;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;


public class PendingNumaMemory extends PendingResource {

    private int nodeIndex;
    private NumaNodeMemoryConsumption memory;

    public PendingNumaMemory(Guid host, VM vm, int nodeIndex, NumaNodeMemoryConsumption memory) {
        super(host, vm);
        this.nodeIndex = nodeIndex;
        this.memory = memory;
    }

    public int getNodeIndex() {
        return nodeIndex;
    }

    public void setNodeIndex(int nodeIndex) {
        this.nodeIndex = nodeIndex;
    }

    public NumaNodeMemoryConsumption getMemoryConsumption() {
        return memory;
    }

    public void setMemoryConsumption(NumaNodeMemoryConsumption memory) {
        this.memory = memory;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        PendingNumaMemory that = (PendingNumaMemory) other;
        return nodeIndex == that.nodeIndex &&
                Objects.equals(getHost(), that.getHost()) &&
                Objects.equals(getVm(), that.getVm());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getHost(), getVm(), nodeIndex);
    }

    public static Map<Integer, NumaNodeMemoryConsumption> collectForHost(PendingResourceManager manager, Guid hostId) {
        return manager.pendingHostResources(hostId, PendingNumaMemory.class).stream()
                .collect(Collectors.toMap(
                        PendingNumaMemory::getNodeIndex,
                        PendingNumaMemory::getMemoryConsumption,
                        NumaNodeMemoryConsumption::merge
                ));
    }
}
