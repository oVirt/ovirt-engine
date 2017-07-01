package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.businessentities.VM;

public class GetVmChangedFieldsForNextRunParameters extends QueryParametersBase {

    private VM original;

    private VM updated;

    private VmManagementParametersBase updateVmParameters;

    public GetVmChangedFieldsForNextRunParameters() {
    }

    public GetVmChangedFieldsForNextRunParameters(VM original,
            VM updated,
            VmManagementParametersBase updateVmParameters) {
        this.original = original;
        this.updated = updated;
        this.updateVmParameters = updateVmParameters;
    }

    public VM getOriginal() {
        return original;
    }

    public VM getUpdated() {
        return updated;
    }

    public void setOriginal(VM original) {
        this.original = original;
    }

    public void setUpdated(VM updated) {
        this.updated = updated;
    }

    public VmManagementParametersBase getUpdateVmParameters() {
        return updateVmParameters;
    }

    public void setUpdateVmParameters(VmManagementParametersBase updateVmParameters) {
        this.updateVmParameters = updateVmParameters;
    }
}
