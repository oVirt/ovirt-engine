package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;

public class CloneVmParameters extends AddVmParameters {

    private Guid newVmGuid;

    private String newName;

    public CloneVmParameters() {

    }

    public CloneVmParameters(VM vm, String newName) {
        super(vm);
        this.newName = newName;
    }

    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }

    public Guid getNewVmGuid() {
        return newVmGuid;
    }

    public void setNewVmGuid(Guid newVmGuid) {
        this.newVmGuid = newVmGuid;
    }
}
