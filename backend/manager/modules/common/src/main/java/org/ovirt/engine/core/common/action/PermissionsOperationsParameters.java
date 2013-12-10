package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.LdapGroup;
import org.ovirt.engine.core.common.businessentities.Permissions;

public class PermissionsOperationsParameters extends VdcActionParametersBase {
    private static final long serialVersionUID = 8854712438369127152L;

    private DbUser user;
    private LdapGroup group;
    private Permissions permission;

    public PermissionsOperationsParameters() {
    }

    public PermissionsOperationsParameters(Permissions permission) {
        setPermission(permission);
    }

    public PermissionsOperationsParameters(Permissions permission, DbUser user) {
        this.permission = permission;
        this.user = user;
    }

    public PermissionsOperationsParameters(Permissions permission, LdapGroup group) {
        this.permission = permission;
        this.group = group;
    }

    public DbUser getUser() {
        return user;
    }

    public void setUser(DbUser value) {
        user = value;
    }

    public LdapGroup getGroup() {
        return group;
    }

    public void setGroup(LdapGroup value) {
        group = value;
    }

    public Permissions getPermission() {
        return permission;
    }

    public void setPermission(Permissions value) {
        permission = value;
    }
}
