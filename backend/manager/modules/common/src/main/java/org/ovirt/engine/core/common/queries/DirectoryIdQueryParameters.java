package org.ovirt.engine.core.common.queries;


/**
 * Parameter class for queries that need a domain name and an identifier.
 */
public class DirectoryIdQueryParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -4231839007150359638L;

    private String domain;
    private String id;

    public DirectoryIdQueryParameters() {
        // Nothing.
    }

    public DirectoryIdQueryParameters(String domain, String id) {
        this.domain = domain;
        this.id = id;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getDomain() {
        return domain;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
