package org.ovirt.engine.core.common.businessentities.aaa;

import org.ovirt.engine.core.common.businessentities.IVdcQueryable;

public class AuthzGroup implements IVdcQueryable {
    private static final long serialVersionUID = -5698641275510275709L;

    private String authz;
    private String namespace;
    private String name;

    public AuthzGroup() {
        super();
    }

    public AuthzGroup(String authz, String namespace, String name) {
        this.authz = authz;
        this.namespace = namespace;
        this.name = name;
    }

    public String getAuthz() {
        return authz;
    }

    public void setAuthz(String authz) {
        this.authz = authz;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Object getQueryableId() {
        return name;
    }

}
