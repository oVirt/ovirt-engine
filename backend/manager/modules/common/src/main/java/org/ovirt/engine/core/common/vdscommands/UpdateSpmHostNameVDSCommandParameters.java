package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class UpdateSpmHostNameVDSCommandParameters extends IrsBaseVDSCommandParameters {

    private String newHostName;
    private String oldHostName;

    public UpdateSpmHostNameVDSCommandParameters(Guid storagePoolId, String oldHostName, String newHostName) {
        super(storagePoolId);
        this.setOldHostName(oldHostName);
        this.setNewHostName(newHostName);
    }

    public void setNewHostName(String newHostName) {
        this.newHostName = newHostName;
    }

    public String getNewHostName() {
        return newHostName;
    }

    public void setOldHostName(String oldHostName) {
        this.oldHostName = oldHostName;
    }

    public String getOldHostName() {
        return oldHostName;
    }

    @Override
    public String toString() {
        return String.format("%s, newHostName = %s, oldHostName = %s",
                super.toString(),
                getNewHostName(),
                getOldHostName());
    }
}
