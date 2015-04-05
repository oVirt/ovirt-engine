package org.ovirt.engine.core.common.action;

import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.validation.annotation.ValidName;
import org.ovirt.engine.core.compat.Guid;

public class VfsConfigLabelParameters extends VfsConfigBaseParameters {

    private static final long serialVersionUID = -918265384359602937L;

    @NotNull
    @ValidName(message = "NETWORK_LABEL_FORMAT_INVALID")
    private String label;

    public VfsConfigLabelParameters() {
    }

    public VfsConfigLabelParameters(Guid nicId, String label) {
        super(nicId);
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
