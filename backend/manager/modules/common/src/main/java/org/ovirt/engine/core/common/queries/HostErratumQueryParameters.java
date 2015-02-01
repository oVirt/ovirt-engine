package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;


public class HostErratumQueryParameters extends IdQueryParameters {

    private static final long serialVersionUID = 4505391636962995236L;
    private String erratumId;

    public HostErratumQueryParameters() {
    }

    public HostErratumQueryParameters(Guid id, String erratumId) {
        super(id);
        this.erratumId = erratumId;
    }

    public String getErratumId() {
        return erratumId;
    }
}
