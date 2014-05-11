package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

public class VmNextRunConfigurationModel extends ConfirmationModel {

    private EntityModel<Boolean> applyCpuLater;
    private boolean cpuPluggable;

    public VmNextRunConfigurationModel() {
        setApplyCpuLater(new EntityModel<Boolean>(false));
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
}
