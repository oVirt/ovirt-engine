package org.ovirt.engine.core.common.businessentities.aaa;

import org.ovirt.engine.core.common.businessentities.Queryable;

public class AuthzGroup implements Queryable {
    private static final long serialVersionUID = -5698641275510275709L;

    private String authz;
    private String namespace;
    private String name;
    private String id;

    public AuthzGroup() {
        super();
    }

    public AuthzGroup(String authz, String namespace, String name) {
        this(authz, namespace, name, null);
    }

    public AuthzGroup(String authz, String namespace, String name, String id) {
        this.authz = authz;
        this.namespace = namespace;
        this.name = name;
        this.id = id;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public Object getQueryableId() {
        return name;
    }

}
