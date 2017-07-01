package org.ovirt.engine.core.common.queries;


/**
 * Parameter class for queries that need a domain name and an identifier.
 */
public class DirectoryIdQueryParameters extends QueryParametersBase {
    private static final long serialVersionUID = -4231839007150359638L;

    private String domain;
    private String id;
    private String namespace;

    public DirectoryIdQueryParameters() {
        // Nothing.
    }

    public DirectoryIdQueryParameters(String domain, String id) {
        this(domain, "", id);
    }

    public DirectoryIdQueryParameters(String domain, String namespace, String id) {
        this.domain = domain;
        this.namespace = namespace;
        this.id = id;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getDomain() {
        return domain;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getId() {
        return id;
    }

    public boolean constainsNamespace() {
        return namespace != null && !namespace.isEmpty();
    }
}
