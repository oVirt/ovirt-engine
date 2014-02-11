package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class GetVmOvfByVmIdParameters extends IdQueryParameters {

    long requiredGeneration;

    public GetVmOvfByVmIdParameters(Guid id, long requiredGeneration) {
        super(id);
        this.requiredGeneration = requiredGeneration;
    }

    public GetVmOvfByVmIdParameters() {
    }

    public long getRequiredGeneration() {
        return requiredGeneration;
    }

    public void setRequiredGeneration(long requiredGeneration) {
        this.requiredGeneration = requiredGeneration;
    }
}
