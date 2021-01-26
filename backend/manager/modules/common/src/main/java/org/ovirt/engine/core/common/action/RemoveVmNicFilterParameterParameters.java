package org.ovirt.engine.core.common.action;

import java.util.Objects;

import org.ovirt.engine.core.compat.Guid;

public class RemoveVmNicFilterParameterParameters extends VmOperationParameterBase {

    private static final long serialVersionUID = 2195472859912525750L;

    // required for GWT
    @SuppressWarnings("FieldMayBeFinal")
    private Guid filterParameterId;

    public RemoveVmNicFilterParameterParameters(Guid vmId, Guid filterParameterId) {
        super(vmId);
        this.filterParameterId = Objects.requireNonNull(filterParameterId);
    }

    public Guid getFilterParameterId() {
        return filterParameterId;
    }

    private RemoveVmNicFilterParameterParameters() {
        filterParameterId = Guid.Empty;
    }
}
