package org.ovirt.engine.core.common.action;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;

public class AddSANStorageDomainParameters extends StorageDomainManagementParameter {
    private static final long serialVersionUID = 6386931158747982426L;
    private List<String> lunIds;

    public List<String> getLunIds() {
        return lunIds == null ? new ArrayList<>() : lunIds;
    }

    public void setLunIds(List<String> value) {
        lunIds = value;
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
