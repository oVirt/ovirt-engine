package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class GetVmOvfByVmIdParameters extends IdQueryParameters {

    long requiredGeneration;
    boolean asOva;

    public GetVmOvfByVmIdParameters(Guid id, long requiredGeneration) {
        super(id);
        this.requiredGeneration = requiredGeneration;
    }

    public GetVmOvfByVmIdParameters(Guid id, long requiredGeneration, boolean asOva) {
        this(id, requiredGeneration);
        this.asOva = asOva;
    }

    public GetVmOvfByVmIdParameters() {
    }

    public long getRequiredGeneration() {
        return requiredGeneration;
    }

    public void setRequiredGeneration(long requiredGeneration) {
        this.requiredGeneration = requiredGeneration;
    }

    public boolean isAsOva() {
        return asOva;
    }

    public void setAsOva(boolean asOva) {
        this.asOva = asOva;
    }
}
