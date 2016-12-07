package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.List;

import org.ovirt.engine.core.common.utils.ToStringBuilder;

public class LeaseStatus implements Serializable {

    private static final long serialVersionUID = 8177640907822845843L;

    private List<Integer> owners;

    public LeaseStatus() {
    }

    public LeaseStatus(List<Integer> owners) {
        this.owners = owners;
    }

    public boolean isFree() {
        return owners != null ? owners.isEmpty() : true;
    }

    public void setOwners(List<Integer> owners) {
        this.owners = owners;
    }

    public List<Integer> getOwners() {
        return owners;
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("owners", owners)
                .build();
    }
}
