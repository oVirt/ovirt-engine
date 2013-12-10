package org.ovirt.engine.core.common.action;

import java.io.Serializable;

import org.ovirt.engine.core.common.businessentities.LdapGroup;

public class AddGroupParameters extends VdcActionParametersBase implements Serializable {
    private static final long serialVersionUID = -3161545951041734975L;

    private LdapGroup group;

    public void setGroup(LdapGroup group) {
        this.group = group;
    }

    public LdapGroup getGroup() {
        return group;
    }

}
