package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class TransferDiskImageParameters extends TransferImageParameters {
    private static final long serialVersionUID = 7834167203208979364L;

    private AddDiskParameters addDiskParameters;

    public TransferDiskImageParameters() {}

    public TransferDiskImageParameters(Guid storageDomainId, AddDiskParameters addDiskParameters) {
        super(storageDomainId);
        this.addDiskParameters = addDiskParameters;
    }

    public AddDiskParameters getAddDiskParameters() {
        return addDiskParameters;
    }

    public void setAddDiskParameters(AddDiskParameters addDiskParameters) {
        this.addDiskParameters = addDiskParameters;
    }
}
