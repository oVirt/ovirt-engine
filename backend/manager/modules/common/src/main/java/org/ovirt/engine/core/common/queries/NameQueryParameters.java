package org.ovirt.engine.core.common.queries;


/**
 * Parameter class for the "GetByName" queries
 */
public class NameQueryParameters extends VdcQueryParametersBase {

    private static final long serialVersionUID = 4281700157334399396L;
    private String name;

    public NameQueryParameters() {
    }

    public NameQueryParameters(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
