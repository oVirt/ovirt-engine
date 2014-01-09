package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.common.utils.ExternalId;

/**
 * Parameter class for queries that need a domain name and an identifier.
 */
public class DirectoryIdQueryParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -4231839007150359638L;

    private String domain;
    private ExternalId id;

    public DirectoryIdQueryParameters() {
        // Nothing.
    }

    public DirectoryIdQueryParameters(String domain, ExternalId id) {
        this.domain = domain;
        this.id = id;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getDomain() {
        return domain;
    }

    public void setId(ExternalId id) {
        this.id = id;
    }

    public ExternalId getId() {
        return id;
    }
}
