package org.ovirt.engine.core.common.action;

import java.util.HashSet;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;

public class AddSANStorageDomainParameters extends StorageDomainManagementParameter {
    private static final long serialVersionUID = 6386931158747982426L;
    private Set<String> lunIds;

    public Set<String> getLunIds() {
        if (lunIds == null) {
            lunIds = new HashSet<>();
        }
        return lunIds;
    }

    public void setLunIds(Set<String> value) {
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
