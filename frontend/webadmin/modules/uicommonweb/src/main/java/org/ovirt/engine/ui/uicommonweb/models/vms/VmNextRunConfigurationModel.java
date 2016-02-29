package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.List;

import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

public class VmNextRunConfigurationModel extends ConfirmationModel {

    private EntityModel<Boolean> applyCpuLater;
    private boolean cpuPluggable;
    private boolean memoryPluggable;
    private List<String> changedFields;
    private boolean vmUnpinned;

    public VmNextRunConfigurationModel() {
        setApplyCpuLater(new EntityModel<>(false));
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

    public EntityModel<Boolean> getApplyCpuLater() {
        return applyCpuLater;
    }

    public void setApplyCpuLater(EntityModel<Boolean> applyCpuLater) {
        this.applyCpuLater = applyCpuLater;
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
}
