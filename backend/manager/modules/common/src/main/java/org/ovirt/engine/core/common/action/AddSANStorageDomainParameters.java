package org.ovirt.engine.core.common.action;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;

public class AddSANStorageDomainParameters extends StorageDomainManagementParameter {
    private static final long serialVersionUID = 6386931158747982426L;
    private ArrayList<String> privateLunIds;

    public ArrayList<String> getLunIds() {
        return privateLunIds == null ? new ArrayList<String>() : privateLunIds;
    }

    public void setLunIds(ArrayList<String> value) {
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
