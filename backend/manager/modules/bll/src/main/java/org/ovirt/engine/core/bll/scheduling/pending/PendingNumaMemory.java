package org.ovirt.engine.core.bll.scheduling.pending;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;


public class PendingNumaMemory extends PendingResource {

    private int nodeIndex;
    private long memoryMB;

    public PendingNumaMemory(Guid host, VM vm, int nodeIndex, long memoryMB) {
        super(host, vm);
        this.nodeIndex = nodeIndex;
        this.memoryMB = memoryMB;
    }

    public int getNodeIndex() {
        return nodeIndex;
    }

    public void setNodeIndex(int nodeIndex) {
        this.nodeIndex = nodeIndex;
    }

    public long getMemoryMB() {
        return memoryMB;
    }

    public void setMemoryMB(long memoryMB) {
        this.memoryMB = memoryMB;
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

    public static Map<Integer, Long> collectForHost(PendingResourceManager manager, Guid hostId) {
        return manager.pendingHostResources(hostId, PendingNumaMemory.class).stream()
                .collect(Collectors.toMap(
                        PendingNumaMemory::getNodeIndex,
                        PendingNumaMemory::getMemoryMB,
                        Long::sum
                ));
    }
}
