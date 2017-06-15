package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.aaa.DbGroup;

public class AddGroupParameters extends ActionParametersBase {

    /**
     *
     */
    private static final long serialVersionUID = -5422503021503559327L;
    private DbGroup groupToAdd;

    public AddGroupParameters() {
    }

    public AddGroupParameters(DbGroup groupToAdd) {
        this.groupToAdd = groupToAdd;
    }

    public DbGroup getGroupToAdd() {
        return groupToAdd;
    }

    public void setGroupToAdd(DbGroup groupToAdd) {
        this.groupToAdd = groupToAdd;
    }

}
