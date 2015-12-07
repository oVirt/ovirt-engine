package org.ovirt.engine.core.common.action;

import java.io.Serializable;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.compat.Guid;

public class RemoveAllVmCinderDisksParameters extends VmOperationParameterBase implements Serializable {

    private boolean invokeEndActionOnParent = true;

    public List<CinderDisk> cinderDisks;

    public RemoveAllVmCinderDisksParameters() {
    }

    public RemoveAllVmCinderDisksParameters(Guid vmId, List<CinderDisk> cinderDisks) {
        super(vmId);
        this.cinderDisks = cinderDisks;
        setForceDelete(false);
    }

    private boolean privateForceDelete;

    public boolean getForceDelete() {
        return privateForceDelete;
    }

    public void setForceDelete(boolean value) {
        privateForceDelete = value;
    }

    public List<CinderDisk> getCinderDisks() {
        return cinderDisks;
    }

    public void setCinderDisks(List<CinderDisk> cinderDisks) {
        this.cinderDisks = cinderDisks;
    }

    public boolean isInvokeEndActionOnParent() {
        return invokeEndActionOnParent;
    }

    public void setInvokeEndActionOnParent(boolean invokeEndActionOnParent) {
        this.invokeEndActionOnParent = invokeEndActionOnParent;
    }
}
