package org.ovirt.engine.core.common.action;

import java.io.Serializable;

import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.LdapUser;
import org.ovirt.engine.core.common.businessentities.LdapGroup;

public class AddUserParameters extends VdcActionParametersBase implements Serializable {
    private static final long serialVersionUID = 3345484510595493227L;

    private DbUser user;

    private LdapGroup adGroup;

    private LdapUser ldapUser;

    public void setUser(DbUser user) {
        this.user = user;
    }

    public DbUser getUser() {
        return user;
    }

    public void setAdGroup(LdapGroup adGroup) {
        this.adGroup = adGroup;
    }

    public LdapGroup getAdGroup() {
        return adGroup;
    }

    public void setAdUser(LdapUser ldapUser) {
        this.ldapUser = ldapUser;
    }

    public LdapUser getAdUser() {
        return ldapUser;
    }

}
