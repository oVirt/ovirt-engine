package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;

import java.util.ArrayList;

public class AddSANStorageDomainParameters extends StorageDomainManagementParameter {
    private static final long serialVersionUID = 6386931158747982426L;
    private java.util.ArrayList<String> privateLunIds;

    public java.util.ArrayList<String> getLunIds() {
        return privateLunIds == null ? new ArrayList<String>() : privateLunIds;
    }

    public void setLunIds(java.util.ArrayList<String> value) {
        privateLunIds = value;
    }

    private boolean force;

    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }

    public AddSANStorageDomainParameters(StorageDomainStatic storageDomain) {
        super(storageDomain);
    }

    public AddSANStorageDomainParameters() {
    }
}
