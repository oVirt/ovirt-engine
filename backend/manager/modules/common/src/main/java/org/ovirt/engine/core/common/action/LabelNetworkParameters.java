package org.ovirt.engine.core.common.action;

import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.validation.annotation.ValidName;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.compat.Guid;

public class LabelNetworkParameters extends UnlabelNetworkParameters {

    private static final long serialVersionUID = -6670273015570157109L;

    @NotNull
    @ValidName(message = "NETWORK_LABEL_FORMAT_INVALID", groups = CreateEntity.class)
    private String label;

    public LabelNetworkParameters() {
    }

    public LabelNetworkParameters(Guid networkId, String label) {
        super(networkId);
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
