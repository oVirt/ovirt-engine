package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;


/**
 * Parameter class for the "GetByName" queries
 */
public class NameQueryParameters extends QueryParametersBase {

    private static final long serialVersionUID = 4281700157334399396L;
    private String name;
    private Guid datacenterId;

    public Guid getDatacenterId() {
        return datacenterId;
    }

    public void setDatacenterId(Guid datacenterId) {
        this.datacenterId = datacenterId;
    }

    public NameQueryParameters() {
    }

    public NameQueryParameters(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
