package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class GetVmTemplatesFromStorageDomainParameters extends IdQueryParameters {

    private static final long serialVersionUID = 690839308028166664L;

    private boolean withDisks;

    public GetVmTemplatesFromStorageDomainParameters() {
    }

    public GetVmTemplatesFromStorageDomainParameters(Guid id, boolean withDisks) {
        super(id);
        this.withDisks = withDisks;
    }

    public boolean isWithDisks() {
        return withDisks;
    }

}
