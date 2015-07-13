package org.ovirt.engine.ui.uicommonweb.models.users;

@SuppressWarnings("unused")
public class UserGroup {
    private String groupName;
    private String authz;
    private String namespace;

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String value) {
        groupName = value;
    }

    public String getAuthz() {
        return authz;
    }

    public void setAuthz(String value) {
        authz = value;
    }
}
