package org.ovirt.engine.core.common.action;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.businessentities.network.VmNicFilterParameter;
import org.ovirt.engine.core.compat.Guid;



public class VmNicFilterParameterParameters extends VmOperationParameterBase {
    private static final long serialVersionUID = -96966637627896053L;

    @Valid
    private VmNicFilterParameter filterParameter;

    public VmNicFilterParameterParameters(Guid vmId, @NotNull VmNicFilterParameter filterParameter) {
        super(vmId);
        this.filterParameter = filterParameter;
    }

    public VmNicFilterParameter getFilterParameter() {
        return filterParameter;
    }

    private VmNicFilterParameterParameters() {
        filterParameter = new VmNicFilterParameter();
    }
}
