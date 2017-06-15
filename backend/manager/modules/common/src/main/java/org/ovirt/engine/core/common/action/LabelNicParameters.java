package org.ovirt.engine.core.common.action;

import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.validation.annotation.ValidName;
import org.ovirt.engine.core.compat.Guid;

public class LabelNicParameters extends ActionParametersBase {

    private static final long serialVersionUID = 657263466320044730L;

    @NotNull
    private Guid nicId;

    @NotNull
    @ValidName(message = "NETWORK_LABEL_FORMAT_INVALID")
    private String label;

    public LabelNicParameters() {
    }

    public LabelNicParameters(Guid nicId, String label) {
        this.setNicId(nicId);
        this.label = label;
    }

    public Guid getNicId() {
        return nicId;
    }

    public void setNicId(Guid nicId) {
        this.nicId = nicId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
