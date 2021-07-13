package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.List;

import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

public class VmNextRunConfigurationModel extends ConfirmationModel {

    private EntityModel<Boolean> applyLater;
    private boolean cpuPluggable;
    private boolean memoryPluggable;
    private boolean vmLeaseUpdated;
    private List<String> changedFields;
    private boolean vmUnpinned;
    private boolean minAllocatedMemoryPluggable;

    public VmNextRunConfigurationModel() {
        setApplyLater(new EntityModel<>(false));
    }

    public boolean isVmUnpinned() {
        return vmUnpinned;
    }

    public void setVmUnpinned() {
        setLatch(new EntityModel<>(false));
        getLatch().setIsAvailable(true);
        getLatch().setIsChangeable(true);
        vmUnpinned = true;
    }

    public EntityModel<Boolean> getApplyLater() {
        return applyLater;
    }

    public void setApplyLater(EntityModel<Boolean> applyLater) {
        this.applyLater = applyLater;
    }

    public boolean isAnythingPluggable() {
        return isCpuPluggable() || isMemoryPluggable() || isVmLeaseUpdated();
    }

    public boolean isCpuPluggable() {
        return cpuPluggable;
    }

    public void setCpuPluggable(boolean cpuPluggable) {
        this.cpuPluggable = cpuPluggable;
    }

    public List<String> getChangedFields() {
        return changedFields;
    }

    public void setChangedFields(List<String> changedFields) {
        this.changedFields = changedFields;
    }

    public boolean isMemoryPluggable() {
        return memoryPluggable;
    }

    public void setMemoryPluggable(boolean memoryPluggable) {
        this.memoryPluggable = memoryPluggable;
    }

    public boolean isVmLeaseUpdated() {
        return vmLeaseUpdated;
    }

    public void setVmLeaseUpdated(boolean vmLeaseUpdated) {
        this.vmLeaseUpdated = vmLeaseUpdated;
    }

    public boolean isMinAllocatedMemoryPluggable() {
        return minAllocatedMemoryPluggable;
    }

    public void setMinAllocatedMemoryPluggable(boolean minAllocatedMemoryPluggable) {
        this.minAllocatedMemoryPluggable = minAllocatedMemoryPluggable;
    }
}
