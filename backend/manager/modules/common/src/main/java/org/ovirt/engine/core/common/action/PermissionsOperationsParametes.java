package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.LdapGroup;
import org.ovirt.engine.core.common.businessentities.Permissions;

public class PermissionsOperationsParametes extends VdcActionParametersBase {
    private static final long serialVersionUID = 8854712438369127152L;

    public PermissionsOperationsParametes(Permissions permission) {
        setPermission(permission);
    }

    public PermissionsOperationsParametes(Permissions permission, DbUser user) {
        this(permission);
        this.user = user;
    }

    public PermissionsOperationsParametes(Permissions permission, LdapGroup adGroup) {
        this(permission);
        setAdGroup(adGroup);
    }

    private DbUser user;

    public DbUser getUser() {
        return user;
    }

    public void setUser(DbUser value) {
        user = value;
    }

    private LdapGroup privateAdGroup;

    public LdapGroup getAdGroup() {
        return privateAdGroup;
    }

    public void setAdGroup(LdapGroup value) {
        privateAdGroup = value;
    }

    private Permissions privatePermission;

    public Permissions getPermission() {
        return privatePermission;
    }

    public void setPermission(Permissions value) {
        privatePermission = value;
    }

    public PermissionsOperationsParametes() {
    }
}
