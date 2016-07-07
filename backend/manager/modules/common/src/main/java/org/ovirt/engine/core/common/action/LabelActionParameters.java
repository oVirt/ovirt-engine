package org.ovirt.engine.core.common.action;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.businessentities.Label;

public class LabelActionParameters extends LabelActionParametersBase {

    @Valid
    @NotNull
    protected Label label;

    private LabelActionParameters() {
    }

    public LabelActionParameters(@NotNull Label label) {
        super(label.getId());
        this.label = label;
    }

    public Label getLabel() {
        return label;
    }
}
